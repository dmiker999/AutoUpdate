package com.example.xiao.autoupdate.update;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * 处理文件下载与线程间通信
 * Created by xiao on 2017/3/3.
 */

public class UpdateDownloadRequest implements Runnable {

    private String mDownloadUrl;
    private String mLocalFilePath;
    private UpdateDownloadListener mListener;
    private boolean isDownloading = false;
    private long currentLength;

    private DownloadResponseHandler downloadHandler;



    public UpdateDownloadRequest(String downloadUrl, String localFilePath, UpdateDownloadListener listener) {
        this.mDownloadUrl = downloadUrl;
        this.mLocalFilePath = localFilePath;
        this.mListener = listener;
        this.isDownloading = true;
        this.downloadHandler = new DownloadResponseHandler();
    }

    /**
     * 真正建立连接的方法
     */
    public void makeRequest() throws IOException,InterruptedException{
        if(!Thread.currentThread().isInterrupted()){
            try{

                URL url = new URL(mDownloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5*1000);
                conn.setRequestProperty("Connection","Keep-Alive");
                conn.connect();
                currentLength = conn.getContentLength();
                if(!Thread.currentThread().isInterrupted()){
                    downloadHandler.sendResponseMessage(conn.getInputStream());
                }
            }catch (Exception e){
                throw e;
            }

        }
    }

    @Override
    public void run() {
        try {
            makeRequest();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 格式化数字
     * @param value
     * @return
     */
    private String getTwoPointFloatStr(float value){
        DecimalFormat format = new DecimalFormat("0.00");
        return format.format(value);
    }

    public enum FailureCode{
        UnknownHost,Socket,SocketTimeout,
        ConnectTimeout,IO,HttpResponse,
        JSON,Interrupted
    }

    /**
     * 真正下载文件，并发送消息和回调接口
     */
    public class DownloadResponseHandler{
        protected static final int SUCCESS_MESSAGE = 0;
        protected static final int FAILURE_MESSAGE = 1;
        protected static final int START_MESSAGE = 2;
        protected static final int FINISH_MESSAGE = 3;
        protected static final int NETWORK_OFF = 4;
        protected static final int PROGRESS_CHANGED = 5;

        private int mCompleteSize = 0;
        private int mProgress = 0;

        private Handler handler;

        public DownloadResponseHandler(){
            handler = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message msg) {
                    handleSelfMessage(msg);

                }
            };
        }

        private void sendFinishMessage(){
            sendMessage(obtainMessage(FINISH_MESSAGE,null));
        }

        private void sendProgressChangedMessage(int progress){
            sendMessage(obtainMessage(PROGRESS_CHANGED,new Object[]{progress}));
        }

        private void sendFailureMessage(FailureCode failureCode){
            sendMessage(obtainMessage(FAILURE_MESSAGE,new Object[]{failureCode}));
        }

        /**
         * 获取一个消息对象
         * @param responseMessage
         * @param response
         * @return
         */
        private Message obtainMessage(int responseMessage, Object response) {
            Message msg = null;
            if(handler!=null){
                msg = handler.obtainMessage(responseMessage,response);
            }else{
                msg = Message.obtain();
                msg.what = responseMessage;
                msg.obj = response;
            }
            return msg;

        }

        private void sendMessage(Message message){
            if(handler!=null){
                handler.sendMessage(message);
            }else{
                handleSelfMessage(message);
            }
        }

        private void handleSelfMessage(Message msg) {
            Object[] response;
            switch (msg.what){
                case FAILURE_MESSAGE:
                    response = (Object[])msg.obj;
                    handleFailureMessage((FailureCode)response[0]);
                    break;
                case PROGRESS_CHANGED:
                    response = (Object[])msg.obj;
                    handleProgressChangedMessage(((Integer)response[0]).intValue());
                    break;
                case FINISH_MESSAGE:
                    onFinish();
                    break;
            }

        }



        protected void handleFailureMessage(FailureCode failureCode) {
            onFailure(failureCode);
        }

        protected void handleProgressChangedMessage(int progress) {
            mListener.onProgressChanged(progress,"");
        }

        protected void onFinish() {
            mListener.onFinished(mCompleteSize,"");

        }

        protected void onFailure(FailureCode code){
            mListener.onFailure();

        }

        /**
         * 文件下载，发送各种类型的事件
         * @param is
         */
        protected void sendResponseMessage(InputStream is){
            RandomAccessFile randomAccessFile = null;
            mCompleteSize = 0;
            try{
                byte[] buffer = new byte[1024];
                int len = -1;
                int limit = 0;
                randomAccessFile = new RandomAccessFile(mLocalFilePath,"rwd");
                while ((len = is.read(buffer)) !=-1){
                    if(isDownloading){
                        randomAccessFile.write(buffer,0,len);
                        mCompleteSize+=len;
                        if(mCompleteSize<currentLength){
                            mProgress = (int) (Float.parseFloat(getTwoPointFloatStr(mCompleteSize/currentLength))*100);
                            if(limit%30 == 0&& mProgress<=100){

                                sendProgressChangedMessage(mProgress);
                            }
                            limit++;
                        }
                    }
                }

                sendFinishMessage();

            }catch (Exception e){
                sendFailureMessage(FailureCode.IO);
            }finally {
                try{
                    if(is!=null){
                        is.close();
                    }
                    if(randomAccessFile!=null){
                        randomAccessFile.close();
                    }
                }catch (IOException e){
                    sendFailureMessage(FailureCode.IO);
                }

            }

        }
    }


}

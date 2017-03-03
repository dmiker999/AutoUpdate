package com.example.xiao.autoupdate.update;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.example.xiao.autoupdate.R;

import java.io.File;

/**
 * app更新下载后台服务
 * Created by xiao on 2017/3/3.
 */

public class UpdateService extends Service {

    private String apkUrl;
    private String filePath;
    private NotificationManager notificationManager;
    private Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/AutoUpdate/app.apk";


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent ==null){
            notifyUser("下载失败","路径错误",-1);
            stopSelf();
        }
        apkUrl = intent.getStringExtra("apkUrl");
        notifyUser("下载开始","开始下载",-1);
        startDownload();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startDownload() {
        UpdateManager.getInstance().startDownload(apkUrl, filePath, new UpdateDownloadListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onProgressChanged(int progress, String downloadUrl) {
                notifyUser("正在下载","正在下载",progress);
            }

            @Override
            public void onFinished(int completeSize, String downloadUrl) {
                notifyUser("下载完成","下载完成",100);
                stopSelf();
            }

            @Override
            public void onFailure() {
                notifyUser("下载失败","路径错误",-1);
                stopSelf();
            }
        });
    }

    /**
     * 更新Notification
     * @param msg
     * @param reason
     * @param progress
     */
    private void notifyUser(String msg,String reason,int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.app_name));

        if(progress<100 &&progress>0){
            builder.setProgress(100,progress,false);
        }else {
            builder.setProgress(0,0,false);
        }
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        builder.setTicker(msg);
        builder.setContentIntent(progress>=100?getContentIntent():
                PendingIntent.getActivity(this,0,new Intent(),PendingIntent.FLAG_UPDATE_CURRENT));
        notification = builder.build();
        notificationManager.notify(0,notification);

    }

    /**
     * 系统安装的Intent
     * @return
     */
    private PendingIntent getContentIntent() {
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://"+apkFile.getAbsolutePath()),
                "application/vnd.android.package-archive");
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;

    }
}

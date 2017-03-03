package com.example.xiao.autoupdate.update;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 下载调度管理器，负责调用UpdateDownloadRequest
 * Created by xiao on 2017/3/3.
 */

public class UpdateManager {

    private static UpdateManager manager;
    private ThreadPoolExecutor mPoolExecutor;
    private UpdateDownloadRequest mRequest;

    private UpdateManager() {
        mPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    public static  UpdateManager getInstance() {
        if (manager == null) {
            synchronized (UpdateManager.class) {
                if (manager == null) {
                    manager = new UpdateManager();
                }
            }
        }

        return manager;
    }

    public void startDownload(String downloadUrl, String downloadFilePath, UpdateDownloadListener listener) {
        if (mRequest != null) {
            return;
        }
        checkLocalFilePath(downloadFilePath);
        mRequest = new UpdateDownloadRequest(downloadUrl, downloadFilePath, listener);
        Future<?> future = mPoolExecutor.submit(mRequest);

    }

    /**
     * 检查文件路径是否已经存在
     * @param path
     */
    private void checkLocalFilePath(String path) {
        File dir = new File(path.substring(0,path.lastIndexOf("/")+1));
        if(!dir.exists())
        {
            dir.mkdir();
        }

        File file = new File(path);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

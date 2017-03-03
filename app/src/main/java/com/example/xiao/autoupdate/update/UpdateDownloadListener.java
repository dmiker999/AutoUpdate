package com.example.xiao.autoupdate.update;

/**
 * Created by xiao on 2017/3/3.
 */

public interface UpdateDownloadListener {

    /**
     * 下载开始
     */
    public void onStart();

    /**
     * 进度的更新回调
     * @param progress
     * @param downloadUrl
     */
    public void onProgressChanged(int progress,String downloadUrl);


    /**
     * 下载完成的回调
     * @param completeSize
     * @param downloadUrl
     */
    public void onFinished(int completeSize,String downloadUrl);

    /**
     * 下载失败的回调
     */
    public void onFailure();
}

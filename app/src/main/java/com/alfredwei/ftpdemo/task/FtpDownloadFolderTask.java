package com.alfredwei.ftpdemo.task;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.alfredwei.ftpdemo.FtpHelper;
import com.alfredwei.ftpdemo.FtpNetCallBack;
import com.alfredwei.ftpdemo.fragment.FtpFragment;
import com.alfredwei.ftpdemo.fragment.LocalFragment;

public class FtpDownloadFolderTask extends AsyncTask<String, Integer, Boolean>
{
    //ftp工具类
    private FtpHelper ftpHelper;
    //回调
    private Fragment[] fragments;
    //ftp目录路径
    private String ftpFolder;
    //本地文件夹路径
    private String localFilePath;

    public FtpDownloadFolderTask(FtpHelper ftpHelper, Fragment[] fragments,
                                 String ftpFolder, String localFilePath) {
        this.ftpHelper = ftpHelper;
        this.fragments = fragments;
        this.ftpFolder = ftpFolder;
        this.localFilePath = localFilePath;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean result = false;
        try {
            if (ftpHelper != null && ftpHelper.isConnect()) {
                int count = ftpHelper.downloadFolder(ftpFolder, localFilePath);
                if (count > 0) {
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        // 下载完成后
        // 通知FtpFragment显示成功
        ((FtpFragment)fragments[0]).downLoadFinish(result);
        // 通知LocalFragment更新listView
        ((LocalFragment)fragments[2]).updateLocalList(((LocalFragment)fragments[2]).getCurrentLocalPath());
    }
}

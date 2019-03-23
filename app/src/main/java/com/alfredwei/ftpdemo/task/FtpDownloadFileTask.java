package com.alfredwei.ftpdemo.task;

import com.alfredwei.ftpdemo.FtpHelper;
import com.alfredwei.ftpdemo.FtpNetCallBack;
import com.alfredwei.ftpdemo.MainActivity;
import com.alfredwei.ftpdemo.fragment.FtpFragment;
import com.alfredwei.ftpdemo.fragment.LocalFragment;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

public class FtpDownloadFileTask extends AsyncTask<String, Integer, Boolean>
{
    //ftp工具类
    private FtpHelper ftpHelper;
    //回调
    private Fragment[] fragments;
    //ftp目录
    private String ftpFolder;
    //本地文件夹
    private String localFilePath;
    //需要下载的文件
    private String fileName;

    public FtpDownloadFileTask(FtpHelper ftpHelper, Fragment[] fragments,
                               String ftpFolder, String fileName, String localFilePath) {
        this.ftpHelper = ftpHelper;
        this.fragments = fragments;
        this.ftpFolder = ftpFolder;
        this.localFilePath = localFilePath;
        this.fileName = fileName;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean result = false;
        try {
            if (ftpHelper != null && ftpHelper.isConnect()) {
                result = ftpHelper.downloadFile(ftpFolder, fileName, localFilePath);
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

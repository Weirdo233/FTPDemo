package com.alfredwei.ftpdemo.task;

import com.alfredwei.ftpdemo.FtpHelper;
import com.alfredwei.ftpdemo.MainActivity;
import com.alfredwei.ftpdemo.fragment.FtpFragment;
import com.alfredwei.ftpdemo.fragment.LocalFragment;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

public class FtpRenameTask extends AsyncTask<String, Integer, Boolean>
{
    //ftp工具类
    private FtpHelper ftpHelper;
    //回调
    private Fragment[] fragments;
    //ftp目录
    private String ftpFolder;
    //原文件名称
    private String oldFileName;
    //新文件名称
    private String newFileName;
    public FtpRenameTask(FtpHelper ftpHelper, Fragment[] fragments,
                         String ftpFolder, String oldFileName, String newFileName) {
        this.ftpHelper = ftpHelper;
        this.fragments = fragments;
        this.ftpFolder = ftpFolder;
        this.oldFileName = oldFileName;
        this.newFileName = newFileName;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean result = false;
        try {
            if (ftpHelper != null && ftpHelper.isConnect()) {
                result = ftpHelper.rename(ftpFolder, oldFileName, newFileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        // 重命名成功后
        // 通知FtpFragment重命名成功
        ((FtpFragment)fragments[0]).renameFinish(result);
        ((FtpFragment)fragments[0]).updateFtpList();
    }
}


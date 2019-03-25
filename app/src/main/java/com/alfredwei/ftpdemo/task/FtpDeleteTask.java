package com.alfredwei.ftpdemo.task;

import com.alfredwei.ftpdemo.FtpHelper;
import com.alfredwei.ftpdemo.MainActivity;
import com.alfredwei.ftpdemo.fragment.FtpFragment;
import com.alfredwei.ftpdemo.fragment.LocalFragment;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

public class FtpDeleteTask extends AsyncTask<String, Integer, Boolean>
{
    //ftp工具类
    private FtpHelper ftpHelper;
    //回调
    private Fragment[] fragments;
    //ftp目录
    private String ftpFolder;
    //需要下载的文件
    private String fileName;

    public FtpDeleteTask(FtpHelper ftpHelper, Fragment[] fragments,
                               String ftpFolder, String fileName) {
        this.ftpHelper = ftpHelper;
        this.fragments = fragments;
        this.ftpFolder = ftpFolder;
        this.fileName = fileName;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean result = true;
        try {
            if (ftpHelper != null && ftpHelper.isConnect()) {
                ftpHelper.delete(ftpFolder, fileName);
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        // 删除成功后
        // 通知FtpFragment删除成功
        ((FtpFragment)fragments[0]).deleteFinish(result);
        ((FtpFragment)fragments[0]).updateFtpList();
    }
}

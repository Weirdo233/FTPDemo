package com.alfredwei.ftpdemo.task;

import android.os.AsyncTask;

import com.alfredwei.ftpdemo.FtpHelper;
import com.alfredwei.ftpdemo.FtpNetCallBack;
import com.alfredwei.ftpdemo.MainActivity;
import com.alfredwei.ftpdemo.fragment.FtpFragment;
import android.support.v4.app.Fragment;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.util.List;

/**
 * 获得FTP当前文件夹下的文件列表
 */
public class FtpFileListTask extends AsyncTask<String, Integer, List<FTPFile>>
{
    //ftp工具类
    private FtpHelper ftpHelper;
    //回调
    private Fragment[] fragments;
    //当前ftp文件夹目录
    private String currentFtpPath;

    public FtpFileListTask(FtpHelper ftpHelper, Fragment[] fragments, String currentFtpPath) {
        this.ftpHelper = ftpHelper;
        this.fragments = fragments;
        this.currentFtpPath = currentFtpPath;
    }

    @Override
    protected List<FTPFile> doInBackground(String... params)
    {
        List<FTPFile> result = null;
        try
        {
            if (ftpHelper != null && ftpHelper.isConnect())
                result = ftpHelper.listFiles(currentFtpPath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(List<FTPFile> result) {
        super.onPostExecute(result);
        //回调FtpFragment更新listView
        ((FtpFragment)fragments[0]).getFtpFileList(result);
    }

}

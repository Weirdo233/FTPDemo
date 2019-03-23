package com.alfredwei.ftpdemo.task;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.alfredwei.ftpdemo.FtpHelper;
import com.alfredwei.ftpdemo.FtpNetCallBack;
import com.alfredwei.ftpdemo.fragment.FtpFragment;
import com.alfredwei.ftpdemo.fragment.GalleryFragment;
import com.alfredwei.ftpdemo.fragment.LocalFragment;

import java.io.File;

/**
 * Created by ZhangHao on 2017/5/19.
 * 上传本地文件（夹）到ftp
 */

public class FtpUploadTask extends AsyncTask<String, Integer, Bundle>
{
    //ftp工具类
    private FtpHelper ftpHelper = null;
    //回调
    private Fragment[] fragments = null;
    //ftp文件夹目录
    private String ftpFolder = null;
    //本地文件夹路径
    //private String localFilePath = null;
    private String[] localFilePaths = null;
    private boolean isFromGallery = false;

    //public FtpUploadTask(FtpHelper ftpHelper, Fragment[] fragments, String localFilePath, String ftpFolder) {
    //    this.ftpHelper = ftpHelper;
    //    this.fragments = fragments;
    //    this.ftpFolder = ftpFolder;
    //    this.localFilePath = localFilePath;
    //}

    public FtpUploadTask(FtpHelper ftpHelper, Fragment[] fragments, String[] localFilePaths, String ftpFolder) {
        this.ftpHelper = ftpHelper;
        this.fragments = fragments;
        this.ftpFolder = ftpFolder;
        this.localFilePaths = localFilePaths;
    }

    public FtpUploadTask(FtpHelper ftpHelper, Fragment[] fragments, String[] localFilePaths, String ftpFolder
    , boolean isFromGallery) {
        this.ftpHelper = ftpHelper;
        this.fragments = fragments;
        this.ftpFolder = ftpFolder;
        this.localFilePaths = localFilePaths;
        this.isFromGallery = isFromGallery;
    }

    @Override
    protected Bundle doInBackground(String... params) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFromGallery", isFromGallery);
        int result = 0;
        try {
            if (ftpHelper != null && ftpHelper.isConnect())
            {
                // 上传多个文件
                for (String localFilePath: localFilePaths)
                {
                    //判断本地文件是否是文件夹
                    File localFile = new File(localFilePath);
                    if (localFile.exists() && localFile.isDirectory()) {
                        //上传文件夹
                        int count = ftpHelper.uploadFolder(localFilePath, ftpFolder);
                        if (count > 0) {
                            //上传数量大于0时
                            result += count;
                        }
                    }
                    else if (localFile.exists() && localFile.isFile())
                    {
                        //上传文件
                        if (ftpHelper.uploadFile(localFilePath, ftpFolder))
                            result++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bundle.putInt("result", result);
        return bundle;
    }

    @Override
    protected void onPostExecute(Bundle bundle) {
        super.onPostExecute(bundle);
        //更新ftp文件listView
        ((FtpFragment)fragments[0]).updateFtpList();
        if (bundle.getBoolean("isFromGallery"))
        {
            //更新GalleryFragment
            ((GalleryFragment) fragments[1]).uploadFinish(bundle.getInt("result"));
            return;
        }
        //给LocalFragment返回结果
        ((LocalFragment)fragments[2]).uploadFinish(bundle.getInt("result"));
    }
}


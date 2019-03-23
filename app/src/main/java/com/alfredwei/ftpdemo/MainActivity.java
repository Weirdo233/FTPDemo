package com.alfredwei.ftpdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alfredwei.ftpdemo.fragment.FtpFragment;
import com.alfredwei.ftpdemo.fragment.GalleryFragment;
import com.alfredwei.ftpdemo.fragment.LocalFragment;
import com.alfredwei.ftpdemo.task.*;

import com.alfredwei.ftpdemo.task.FtpFileListTask;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
{

    //当前ftp路径
    private String currentFtpPath = FtpHelper.REMOTE_PATH;
    //当前本地路径
    private String currentLocalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    //ftp工具类
    FtpHelper ftp;

    //Record which fragment is currently shown.
    private int currentFragment = 0;
    private Fragment[] fragments = {new FtpFragment(), new GalleryFragment(), new LocalFragment()};

    private final int REQUEST_WRITE_EXTERNAL_STORAGE = 5556;
    private void checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE))
            {
                Toast.makeText(this, "Please check your storage permission", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);

        }
        else {
        }
    }

    private void switchFragment(int currentFragment, int index)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!fragments[index].isAdded())
        {
            transaction
                    .hide(fragments[currentFragment])
                    .add(R.id.fragment_container, fragments[index])
                    .show(fragments[index])
                    .commit();
        }
        else {
            transaction
                    .hide(fragments[currentFragment])
                    .show(fragments[index])
                    .commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.navigation_server:
                    if (currentFragment != 0)
                    {
                        switchFragment(currentFragment, 0);
                        currentFragment = 0;
                    }
                    return true;
                case R.id.navigation_gallery:
                    if (currentFragment != 1)
                    {
                        switchFragment(currentFragment, 1);
                        currentFragment = 1;
                    }
                    return true;
                case R.id.navigation_local:
                    if (currentFragment != 2)
                    {
                        switchFragment(currentFragment, 2);
                        currentFragment = 2;
                    }
                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        checkPermission();
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //initFtp();
        switchFragment(currentFragment, 0);
        //initFtpList();
        //initLocalList();
        //updateLocalList(localPath);
    }

    public void setCurrentFtpPath(String path)
    {
        this.currentFtpPath = path;
    }

    public String getCurrentFtpPath()
    {
        return this.currentFtpPath;
    }

    public void setCurrentLocalPath(String path)
    {
        this.currentLocalPath = path;
    }

    public String getCurrentLocalPath()
    {
        return this.currentLocalPath;
    }

    public void setFtp(FtpHelper ftp)
    {
        this.ftp = ftp;
    }

    public FtpHelper getFtp()
    {
        return this.ftp;
    }

    public Fragment[] getFragments()
    {
        return fragments;
    }

    //显示Toast
    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private boolean mIsExit = false;

    /**
     * 按键事件监听
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            //ftp文件返回上一层
            if (!currentFtpPath.equals(FtpHelper.REMOTE_PATH) && currentFragment == 0)
            {
                currentFtpPath = currentFtpPath.substring(0, currentFtpPath.lastIndexOf("/"));
                FtpFragment ftpFragment = (FtpFragment) fragments[0];
                ftpFragment.setCurrentFtpPath(currentFtpPath);
                ftpFragment.updateFtpList();
            }
            //本地文件返回上一层
            else if (!currentLocalPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath())
                    && currentFragment == 2)
            {
                currentLocalPath = currentLocalPath.substring(0, currentLocalPath.lastIndexOf("/"));
                LocalFragment localFragment = (LocalFragment) fragments[2];
                localFragment.setCurrentFtpPath(currentLocalPath);
                localFragment.updateLocalList(currentLocalPath);
            }
            //处在根目录，两次按下返回键退出
            else
            {
                if (mIsExit)
                {
                    this.finish();

                }
                else
                {
                    Toast.makeText(this, "Tap again to quit.", Toast.LENGTH_SHORT).show();
                    mIsExit = true;
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mIsExit = false;
                        }
                    }, 2000);
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}




package com.alfredwei.ftpdemo;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
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

    private final String host = "192.168.129.1";
    private final String userName = "FtpUser";
    private final String passWord = "112233";

    //Record which fragment is currently shown.
    private int currentFragment = 0;
    private Fragment[] fragments = {new FtpFragment(), new GalleryFragment(), new LocalFragment()};
    private ImageView img_status;
    private Toolbar toolbar;
    private ListView listView;
    private BottomNavigationView navigation;

    private boolean isInMultiSelect = false;
    private boolean isRegistered = false;
    private NetWorkChangReceiver netWorkChangReceiver;

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
                        ((FtpFragment) fragments[0]).updateFtpList();
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
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        initFtp();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, fragments[1]).hide(fragments[1]);
        transaction.add(R.id.fragment_container, fragments[2]).hide(fragments[2]);
        transaction.commit();

        initToolbar();

        switchFragment(currentFragment, 0);

        //监听网络状态
        initNetWorkReceiver();
    }

    private void initFtp()
    {
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                {
                    try {
                        ftp = new FtpHelper(host, userName,
                                passWord);
                        ftp.openConnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void initToolbar()
    {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.multiselect:
                        // GalleryFragment不允许多选
                        if (currentFragment == 1)
                            break;
                        // 进入多选模式
                        initMultiSelectToolbar();
                        break;
                }
                return true;
            }
        });
    }

    private void initMultiSelectToolbar()
    {
        navigation.setVisibility(View.INVISIBLE);
        isInMultiSelect = true;

        toolbar.getMenu().findItem(R.id.delete).setVisible(true);
        toolbar.getMenu().findItem(R.id.multiselect).setVisible(false);
        toolbar.setNavigationIcon(R.drawable.back_arrow);
        //TODO:进入多选listView
        if (currentFragment == 2)
        {
            toolbar.getMenu().findItem(R.id.upload).setVisible(true);
            ((LocalFragment) fragments[currentFragment]).initCheckList();
        }
        else if (currentFragment == 0)
        {
            toolbar.getMenu().findItem(R.id.download).setVisible(true);
            ((FtpFragment) fragments[currentFragment]).initCheckList();
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                returnToToolbar();
            }
        });
    }

    public void returnToToolbar()
    {
        isInMultiSelect = false;
        // 重新响应导航栏
        toolbar.getMenu().findItem(R.id.delete).setVisible(false);
        toolbar.getMenu().findItem(R.id.multiselect).setVisible(true);
        // 取消NavigationIcon
        toolbar.setNavigationIcon(null);
        // 显示底部导航栏
        navigation.setVisibility(View.VISIBLE);
        if (currentFragment == 2)
        {
            toolbar.getMenu().findItem(R.id.upload).setVisible(false);
            ((LocalFragment) fragments[currentFragment]).initLocalList();
        }
        else if (currentFragment == 0)
        {
            ((FtpFragment) fragments[currentFragment]).initFtpList();
            toolbar.getMenu().findItem(R.id.download).setVisible(false);
        }
    }

    public Toolbar getToolbar()
    {
        return toolbar;
    }

    private void initNetWorkReceiver()
    {
        //注册网络状态监听广播
        netWorkChangReceiver = new NetWorkChangReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangReceiver, filter);
        isRegistered = true;

    }

    public boolean isNetworkConnected()
    {
        ConnectivityManager mConnectivityManager =
                    (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
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

    public void changeConnectState(boolean objectStatus)
    {
        if (objectStatus == false)
        {
            //img_status.setImageResource(R.drawable.disconnected);
            toolbar.getMenu().getItem(0).setIcon(R.drawable.disconnected);
            //img_status.setTag("disconnected");
            showToast("Connection fail, please check your internet setting");
            // 登出ftp服务器
            //new Thread(new Runnable() {
            //    @Override
            //    public void run() {
            //        if (ftp != null) {
            //            try {
            //                ftp.closeConnect();
            //                ftp = null;
            //            } catch (IOException e) {
            //                e.printStackTrace();
            //            }
            //        }
            //    }
            //}).start();
        }
        else if (objectStatus == true)
        {
            // 重新连接并更新各fragment的ftp引用
            try{
                toolbar.getMenu().getItem(0).setIcon(R.drawable.connected);
                new Thread(new Runnable() {
                    @Override
                    public void run()
                    {
                        {
                            try {
                                ftp = new FtpHelper(host, userName,
                                        passWord);
                                ftp.openConnect();
                                ((FtpFragment) fragments[0]).initFtp();
                                ((FtpFragment) fragments[0]).updateFtpList();
                                ((GalleryFragment) fragments[1]).initFtp();
                                ((LocalFragment) fragments[2]).initFtp();
                                //img_status.setImageResource(R.drawable.connected);
                                //img_status.setTag("connected");
                                //showToast("Reconnect successfully");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
            catch (Exception e)
            {
                showToast("Reconnect fail");
                e.printStackTrace();
            }
        }
    }

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
            if (isInMultiSelect)
            {
                returnToToolbar();
            }
            //ftp文件返回上一层
            else if (!currentFtpPath.equals(FtpHelper.REMOTE_PATH) && currentFragment == 0)
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
                localFragment.setCurrentLocalPath(currentLocalPath);
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
    protected void onDestroy()
    {
        super.onDestroy();
        // 登出ftp服务器
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ftp != null) {
                    try {
                        ftp.closeConnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        Log.d("FtpFragment", "onDestroy is called.");
        Log.d("FtpClient", "Logout FTP server.");
        //解绑
        if (isRegistered) {
            unregisterReceiver(netWorkChangReceiver);
        }
    }
}




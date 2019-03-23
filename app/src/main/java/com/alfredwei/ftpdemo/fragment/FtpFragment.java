package com.alfredwei.ftpdemo.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.alfredwei.ftpdemo.FtpFileAdapter;
import com.alfredwei.ftpdemo.FtpHelper;
import com.alfredwei.ftpdemo.MainActivity;
import com.alfredwei.ftpdemo.R;
import com.alfredwei.ftpdemo.task.FtpDownloadFileTask;
import com.alfredwei.ftpdemo.task.FtpDownloadFolderTask;
import com.alfredwei.ftpdemo.task.FtpFileListTask;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FtpFragment extends Fragment
{
    private ListView ftpListView;
    private List<FTPFile> ftpList = new ArrayList<>();
    private FtpFileAdapter ftpAdapter;

    private FtpHelper ftp;
    //当前ftp路径
    private String currentFtpPath = FtpHelper.REMOTE_PATH;
    //文件下载路径
    private String localPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/FTPDemo";

    public FtpFragment() {
        //Required empty constructor
    }

    public void setCurrentFtpPath(String string)
    { this.currentFtpPath = string; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.ftp_fragment, container,false);
        ftpListView = (ListView) view.findViewById(R.id.listView);
        initFtp();
        initFtpList();
        Log.d("FtpFragment", "onCreateView is called.");
        //Return the view
        return view;
    }

    //初始化FtpClient
    private void initFtp()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ftp == null) {
                    try {
                        ftp = new FtpHelper("192.168.129.1", "FtpUser",
                                "112233");
                        ftp.openConnect();
                        ((MainActivity) getActivity()).setFtp(ftp);
                        //更新ftp文件列表
                        updateFtpList();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    //初始化ListView相关
    private void initFtpList()
    {
        ftpAdapter = new FtpFileAdapter(this.getContext(), R.layout.item_layout, ftpList);
        ftpListView.setAdapter(ftpAdapter);
        //点击进入文件夹
        ftpListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                FTPFile file = ftpList.get(position);
                if (file.isDirectory())
                {
                    currentFtpPath = currentFtpPath + "/" + file.getName();
                    ((MainActivity) getActivity()).setCurrentFtpPath(currentFtpPath);
                    updateFtpList();
                }
            }
        });

        //长按下载
        ftpListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                // TODO:弹出Dialog
                FTPFile file = ftpList.get(position);
                if (file.isDirectory()) {
                    downLoadFolder(file.getName());
                } else if (file.isFile()) {
                    downLoadFile(file.getName());
                }
                return true;
            }
        });

    }
    //创建异步线程，更新ftp文件列表
    public void updateFtpList()
    {
        new FtpFileListTask(ftp, ((MainActivity) getActivity()).getFragments(), currentFtpPath).execute();
    }

    //在listView中刷新文件列表
    public void getFtpFileList(List<FTPFile> ftpFileList) {
        if (ftpFileList != null) {
            ftpList.clear();
            ftpList.addAll(ftpFileList);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ftpAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    //下载文件
    private void downLoadFile(String fileName)
    {
        showToast("Download " + fileName);
        new FtpDownloadFileTask(ftp, ((MainActivity) getActivity()).getFragments(), currentFtpPath, fileName, localPath).execute();
    }

    //下载文件夹
    private void downLoadFolder(String folderName)
    {
        showToast("Download " + folderName);
        new FtpDownloadFolderTask(ftp, ((MainActivity) getActivity()).getFragments(), currentFtpPath + "/" + folderName, localPath).execute();
    }

    public void downLoadFinish(boolean result) {
        if (result)
        {
            showToast("Download successfully");
        }
        else
        {
            showToast("Download fail or folder is empty");
        }
    }

    private void showToast(String string)
    {
        Toast.makeText(getActivity().getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy()
    {
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
        super.onDestroy();
    }
}

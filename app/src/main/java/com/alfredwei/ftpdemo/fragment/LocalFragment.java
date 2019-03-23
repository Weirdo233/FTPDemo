package com.alfredwei.ftpdemo.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.alfredwei.ftpdemo.FtpFileAdapter;
import com.alfredwei.ftpdemo.FtpHelper;
import com.alfredwei.ftpdemo.LocalFileAdapter;
import com.alfredwei.ftpdemo.MainActivity;
import com.alfredwei.ftpdemo.R;
import com.alfredwei.ftpdemo.task.FtpFileListTask;
import com.alfredwei.ftpdemo.task.FtpUploadTask;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalFragment extends Fragment
{
    private ListView localListView;
    private List<File> localList = new ArrayList<>();
    private LocalFileAdapter localAdapter;
    private FtpHelper ftp;
    //当前ftp路径
    private String currentFtpPath = FtpHelper.REMOTE_PATH;
    //当前本地路径
    private String currentLocalPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    public LocalFragment() {
        //Required empty constructor
    }

    public void setCurrentLocalPath(String string)
    { currentLocalPath = string; }
    public String getCurrentFtpPath()
    { return currentFtpPath; }
    public String getCurrentLocalPath()
    { return currentLocalPath; }
    public void setCurrentFtpPath(String string)
    { currentFtpPath = string; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.local_fragment, container,false);
        localListView = (ListView) view.findViewById(R.id.listView);
        this.ftp = ((MainActivity) getActivity()).getFtp();
        initLocalList();
        //Return the view
        return view;

    }

    private void initLocalList()
    {
        localAdapter = new LocalFileAdapter(getContext(), R.layout.item_layout, localList);
        localListView.setAdapter(localAdapter);
        //点击进入文件夹
        localListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = localList.get(position);
                if (file.isDirectory()) {
                    ((MainActivity) getActivity()).setCurrentLocalPath(file.getAbsolutePath());
                    updateLocalList(file.getAbsolutePath());
                }
            }
        });
        updateLocalList(currentLocalPath);

         //长按上传
         localListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            upload(localList.get(position).getAbsolutePath());
            return true;
            }
            });

    }

    //上传
    private void upload(String localFilePath) {
        new FtpUploadTask(ftp, ((MainActivity) getActivity()).getFragments(), localFilePath, currentFtpPath).execute();
    }

    public void uploadFinish(boolean result)
    {
        if (result)
        {
            showToast("Upload successfully");
        } else {
            showToast("Upload an empty folder or fail");
        }
    }


    // 更新当前本地文件列表
    public void updateLocalList(String path)
    {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            //获取该文件夹下所有文件
            File[] files = file.listFiles();
            localList.clear();
            Collections.addAll(localList, files);
            localAdapter.notifyDataSetChanged();
            currentLocalPath = path;
        }
    }

    private void showToast(String string)
    {
        Toast.makeText(getActivity().getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }
}

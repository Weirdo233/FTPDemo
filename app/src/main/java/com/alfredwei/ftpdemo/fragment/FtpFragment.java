package com.alfredwei.ftpdemo.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.alfredwei.ftpdemo.FtpFileAdapter;
import com.alfredwei.ftpdemo.FtpFileCheckAdapter;
import com.alfredwei.ftpdemo.FtpHelper;
import com.alfredwei.ftpdemo.LocalFileCheckAdapter;
import com.alfredwei.ftpdemo.MainActivity;
import com.alfredwei.ftpdemo.R;
import com.alfredwei.ftpdemo.task.FtpDeleteTask;
import com.alfredwei.ftpdemo.task.FtpDownloadFileTask;
import com.alfredwei.ftpdemo.task.FtpDownloadFolderTask;
import com.alfredwei.ftpdemo.task.FtpFileListTask;
import com.alfredwei.ftpdemo.task.FtpRenameTask;

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
    private String localPath;

    public FtpFragment() {
        //Required empty constructor
    }

    public void setCurrentFtpPath(String string)
    { this.currentFtpPath = string; }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        localPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.app_name);
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
    public void initFtp()
    {
        this.ftp = ((MainActivity) getActivity()).getFtp();
    }
    //初始化ListView相关
    public void initFtpList()
    {
        updateFtpList();
        ftpAdapter = new FtpFileAdapter(this.getContext(), R.layout.item_layout, ftpList);
        ftpListView.setAdapter(ftpAdapter);
        //点击进入文件夹
        ftpListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!isNetworkConnected())
                {
                    showToast("No connection");
                    return;
                }
                if (ftp == null)
                    initFtp();
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
        ftpListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
            {
                if (!isNetworkConnected())
                {
                    showToast("No connection");
                    return true;
                }
                if (ftp == null)
                    initFtp();
                final AlertDialog.Builder bulder = new AlertDialog.Builder(getContext());
                View view1 = View.inflate(getContext(), R.layout.ftp_dialog_layout, null);
                bulder.setTitle(ftpList.get(position).getName())
                        .setView(view1)
                        .create();
                Button btn_download = (Button) view1.findViewById(R.id.btn_download);
                Button btn_delete = (Button) view1.findViewById(R.id.btn_delete);
                Button btn_rename = (Button) view1.findViewById(R.id.btn_rename);

                final AlertDialog dialog = bulder.show();
                btn_download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FTPFile file = ftpList.get(position);
                        if (file.isDirectory()) {
                            downLoadFolder(file.getName());
                        } else if (file.isFile()) {
                            downLoadFile(file.getName());
                        }
                        dialog.dismiss();
                    }
                });
                btn_delete.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        FTPFile file = ftpList.get(position);
                        delete(file.getName());
                        dialog.dismiss();
                    }
                });
                btn_rename.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String path = currentFtpPath + "/" + ftpList.get(position).getName();
                        final String parentPath = currentFtpPath;
                        final String oldFileName = path.substring(path.lastIndexOf("/") + 1);
                        // 修改文件名Dialog
                        final AlertDialog.Builder bulder = new AlertDialog.Builder(getContext());
                        View view1 = View.inflate(getContext(), R.layout.rename_dialog_layout, null);
                        final EditText edt_newFileName = (EditText) view1.findViewById(R.id.new_file_name);
                        edt_newFileName.setText(oldFileName);
                        bulder.setTitle("Rename")
                                .setView(view1)
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        if (edt_newFileName.getText().length() == 0)
                                        {
                                            showToast("File name cannot be empty");return;
                                        }
                                        if (edt_newFileName.getText().equals(oldFileName))
                                        {
                                            showToast("File or directory already exist");return;
                                        }
                                        String newFileName = edt_newFileName.getText().toString();
                                        rename(oldFileName, newFileName);
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                    }
                                })
                                .create();
                        final AlertDialog dialog1 = bulder.show();
                    }
                });


                return true;
            }
        });

    }

    public void initCheckList()
    {
        FtpFileCheckAdapter adapter = new FtpFileCheckAdapter(getContext(), R.layout.check_item_layout, ftpList);
        ftpListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ftpListView.setAdapter(adapter);
        ftpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                CheckedTextView item = view.findViewById(R.id.checkedTextView);
                item.toggle();
            }
        });
        updateFtpList();
        ((MainActivity) getActivity()).getToolbar().getMenu().findItem(R.id.download)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem)
                    {
                        if (!isNetworkConnected())
                        {
                            showToast("No connection");
                            return false;
                        }
                        ArrayList<String> paths = new ArrayList<>();
                        SparseBooleanArray  checkedItemPositions = ftpListView.getCheckedItemPositions();
                        //循环遍历集合中所有的数据，获取每个item是否在SparseBooleanArray存储，以及对应的值；
                        for (int i = 0; i < ftpList.size(); i++) {
                            //根据key获取对应的boolean值，为true则下载
                            if (checkedItemPositions.get(i))
                            {
                                FTPFile file = ftpList.get(i);
                                if (file.isDirectory()) {
                                    downLoadFolder(file.getName());
                                } else if (file.isFile()) {
                                    downLoadFile(file.getName());
                                }
                            }
                        }
                        ((MainActivity) getActivity()).returnToToolbar();
                        return false;
                    }
                });

        ((MainActivity) getActivity()).getToolbar().getMenu().findItem(R.id.delete)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem)
                    {
                        if (!isNetworkConnected())
                        {
                            showToast("No connection");
                            return false;
                        }
                        ArrayList<String> paths = new ArrayList<>();
                        SparseBooleanArray checkedItemPositions = ftpListView.getCheckedItemPositions();
                        //循环遍历集合中所有的数据，获取每个item是否在SparseBooleanArray存储，以及对应的值；
                        for (int i = 0; i < ftpList.size(); i++) {
                            //根据key获取对应的boolean值，为true则加入paths
                            if (checkedItemPositions.get(i))
                                delete(ftpList.get(i).getName());
                        }
                        ((MainActivity) getActivity()).returnToToolbar();
                        return false;
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

    private void delete(String fileName)
    {
        new FtpDeleteTask(ftp, ((MainActivity) getActivity()).getFragments(), currentFtpPath, fileName).execute();
    }

    private void rename(String oldFileName, String newFileName)
    {
        new FtpRenameTask(ftp, ((MainActivity) getActivity()).getFragments(), currentFtpPath, oldFileName, newFileName).execute();
    }


    public void downLoadFinish(boolean result) {
        if (result)
            showToast("Download successfully");
        else
            showToast("Download fail or folder is empty");
    }

    public void deleteFinish(boolean result)
    {
        if (!result)
            showToast("Delete fail");
    }

    public void renameFinish(boolean result)
    {
        if (!result)
            showToast("Rename fail");
    }

    private void showToast(String string)
    {
        Toast.makeText(getActivity().getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

    private boolean isNetworkConnected()
    {
        return ((MainActivity) getActivity()).isNetworkConnected();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}

package com.alfredwei.ftpdemo.fragment;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import android.widget.ListView;
import android.widget.Toast;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;

import com.alfredwei.ftpdemo.FtpFileAdapter;
import com.alfredwei.ftpdemo.FtpHelper;
import com.alfredwei.ftpdemo.LocalFileAdapter;
import com.alfredwei.ftpdemo.MainActivity;
import com.alfredwei.ftpdemo.R;
import com.alfredwei.ftpdemo.task.FtpFileListTask;
import com.alfredwei.ftpdemo.task.FtpUploadTask;
import com.alfredwei.ftpdemo.LocalFileCheckAdapter;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Array;
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
        //View view = inflater.inflate(R.layout.c, container,false);
        localListView = (ListView) view.findViewById(R.id.listView);

        initFtp();
        initLocalList();
        //Return the view
        return view;

    }



    public void initFtp()
    {
        this.ftp = ((MainActivity) getActivity()).getFtp();
    }

    public void initLocalList()
    {
        localAdapter = new LocalFileAdapter(getContext(), R.layout.item_layout, localList);

        localListView.setAdapter(localAdapter);
        //点击进入文件夹
        localListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = localList.get(position);
                if (file.isDirectory())
                {
                    ((MainActivity) getActivity()).setCurrentLocalPath(file.getAbsolutePath());
                    updateLocalList(file.getAbsolutePath());
                }
                else
                {
                    //openFile(getContext(), file.getAbsolutePath());
                }
            }
        });
        updateLocalList(currentLocalPath);

         //长按
         localListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id)
            {
                if (ftp == null)
                    initFtp();
                //弹出Dialog
                final AlertDialog.Builder bulder = new AlertDialog.Builder(getContext());
                View view1 = View.inflate(getContext(), R.layout.local_dialog_layout, null);
                bulder.setTitle(localList.get(position).getName())
                        .setView(view1)
                        .create();
                Button btn_upload = (Button) view1.findViewById(R.id.btn_upload);
                Button btn_delete = (Button) view1.findViewById(R.id.btn_delete);
                Button btn_rename = (Button) view1.findViewById(R.id.btn_rename);
                final AlertDialog dialog = bulder.show();
                btn_upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isNetworkConnected())
                        {
                            showToast("No connection");
                            return ;
                        }
                        upload(localList.get(position).getAbsolutePath());
                        dialog.dismiss();
                    }
                });
                btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String path = localList.get(position).getAbsolutePath();
                        delete(path);
                        path = path.substring(0, path.lastIndexOf("/"));
                        updateLocalList(path);
                        dialog.dismiss();
                    }
                });
                btn_rename.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String path = localList.get(position).getAbsolutePath();
                        final String parentPath = currentLocalPath;
                        final String oldFileName = localList.get(position).getName();
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
                                        String newPath = parentPath + "/" + edt_newFileName.getText();
                                        if (!renameFileOrFolder(path, newPath))
                                            showToast("Rename fail");
                                        else
                                        {
                                            updateLocalList(parentPath);
                                            dialog.dismiss();
                                        }
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
        LocalFileCheckAdapter adapter = new LocalFileCheckAdapter(getContext(), R.layout.check_item_layout, localList);
        localListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        localListView.setAdapter(adapter);
        localListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                CheckedTextView item = view.findViewById(R.id.checkedTextView);
                item.toggle();
            }
        });
        updateLocalList(currentLocalPath);

        // Upload按钮
        ((MainActivity) getActivity()).getToolbar().getMenu().findItem(R.id.upload)
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
                        SparseBooleanArray  checkedItemPositions = localListView.getCheckedItemPositions();
                        //循环遍历集合中所有的数据，获取每个item是否在SparseBooleanArray存储，以及对应的值；
                        for (int i = 0; i < localList.size(); i++) {
                            //根据key获取对应的boolean值，为true则加入paths
                            if (checkedItemPositions.get(i))
                                paths.add(localList.get(i).getAbsolutePath());
                        }
                        upload((String[])paths.toArray(new String[paths.size()]));
                        ((MainActivity) getActivity()).returnToToolbar();
                        return false;
                    }
                });

        // Delete按钮
        ((MainActivity) getActivity()).getToolbar().getMenu().findItem(R.id.delete)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem)
                    {
                        ArrayList<String> paths = new ArrayList<>();
                        SparseBooleanArray  checkedItemPositions = localListView.getCheckedItemPositions();
                        //循环遍历集合中所有的数据，获取每个item是否在SparseBooleanArray存储，以及对应的值；
                        for (int i = 0; i < localList.size(); i++) {
                            //根据key获取对应的boolean值，为true则加入paths
                            if (checkedItemPositions.get(i))
                                paths.add(localList.get(i).getAbsolutePath());
                        }
                        // 删除选中文件
                        for (int i = 0; i < paths.size(); i++)
                            delete(paths.get(i));
                        // 更新父路径文件列表
                        String parentPath = paths.get(0).substring(0, paths.get(0).lastIndexOf("/"));
                        updateLocalList(parentPath);
                        ((MainActivity) getActivity()).returnToToolbar();
                        return false;
                    }
                });
    }

    /**
     * 修改文件名
     * @param oldFilePath 原文件路径
     * @param newFilePath 新文件名称
     * @return
     */
    public static boolean renameFileOrFolder(String oldFilePath, String newFilePath)
    {
        File oldFile = new File(oldFilePath);
        if(!oldFile.exists())
        {
            return false;
        }
        try
        {
            if (oldFile.isFile())
            {
                String newFilepath = oldFile.getParent() + File.separator + newFilePath;
                FileUtils.copyFile(oldFile, new File(newFilePath));
                FileUtils.deleteQuietly(oldFile);
                return true;
            } else
            {
                FileUtils.copyDirectory(oldFile, new File(newFilePath));
                FileUtils.deleteDirectory(oldFile);
                return true;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    //上传
    private void upload(String... localFilePath)
    {
        String[] filePaths = localFilePath;
        new FtpUploadTask(ftp, ((MainActivity) getActivity()).getFragments(), filePaths, currentFtpPath)
                .execute();
    }


    public void uploadFinish(Integer result)
    {
        if (result > 0)
        {
            showToast("Upload " + result + " files successfully");
        }
        else {
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

    public static boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return false;
        }
        return FileUtils.deleteQuietly(file);
    }

    private boolean isNetworkConnected()
    {
        return ((MainActivity) getActivity()).isNetworkConnected();
    }

    private void showToast(String string)
    {
        Toast.makeText(getActivity().getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

    public void openFile(Context context, String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(context,"com.alfredwei.ftpdemo.fileprovider", file);

            intent.setData(/*uri*/uri);
            context.startActivity(intent);
            Intent.createChooser(intent, "请选择对应的软件打开该附件！");

        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Cannot open this file, please install relative appication", Toast.LENGTH_SHORT)
                    .show();
        }
    }
    private String getMIMEType(File file)
    {
        String type = "*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }

        /* 获取文件的后缀名*/
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++)
        {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    private static final String[][] MIME_MapTable = {
            //{后缀名，MIME类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };
}

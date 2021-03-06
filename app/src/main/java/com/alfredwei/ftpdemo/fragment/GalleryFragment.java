package com.alfredwei.ftpdemo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.alfredwei.ftpdemo.FtpHelper;
import com.alfredwei.ftpdemo.MainActivity;
import com.alfredwei.ftpdemo.MediaAdapter;
import com.alfredwei.ftpdemo.R;
import com.alfredwei.ftpdemo.task.FtpUploadTask;
import com.guoxiaoxing.phoenix.core.PhoenixOption;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;
import com.guoxiaoxing.phoenix.core.model.MimeType;
import com.guoxiaoxing.phoenix.picker.Phoenix;


import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment implements MediaAdapter.OnAddMediaListener
        , View.OnClickListener {

    private int REQUEST_CODE = 0x000111;
    private MediaAdapter mMediaAdapter;

    private FtpHelper ftp;
    //当前ftp路径
    private String currentFtpPath = FtpHelper.REMOTE_PATH;

    public static GalleryFragment newInstance(){
        return new GalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity().getWindow() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_fragment, container, false);
        view.findViewById(R.id.btn_upload).setOnClickListener(this);
        //view.findViewById(R.id.btn_compress_video).setOnClickListener(this);
        //view.findViewById(R.id.btn_take_picture).setOnClickListener(this);

        initFtp();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4, GridLayoutManager.VERTICAL, false));
        mMediaAdapter = new MediaAdapter(this);
        recyclerView.setAdapter(mMediaAdapter);
        mMediaAdapter.setOnItemClickListener(new MediaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (mMediaAdapter.getData().size() > 0) {
                    //预览
                    Phoenix.with()
                            .pickedMediaList(mMediaAdapter.getData())
                            .start(getActivity(), PhoenixOption.TYPE_BROWSER_PICTURE, 0);
                }
            }
        });
        return view;
    }

    public void initFtp()
    {
        this.ftp = ((MainActivity) getActivity()).getFtp();
    }

    @Override
    public void onAddMedia() {
        Phoenix.with()
                .theme(PhoenixOption.THEME_DEFAULT)// 主题
                .fileType(MimeType.ofAll())//显示的文件类型图片、视频、图片和视频
                .maxPickNumber(9)// 最大选择数量
                .minPickNumber(0)// 最小选择数量
                .spanCount(4)// 每行显示个数
                .enablePreview(true)// 是否开启预览
                .enableCamera(true)// 是否开启拍照
                .enableAnimation(true)// 选择界面图片点击效果
                .enableCompress(true)// 是否开启压缩
                .compressPictureFilterSize(1024)//多少kb以下的图片不压缩
                .compressVideoFilterSize(2018)//多少kb以下的视频不压缩
                .thumbnailHeight(160)// 选择界面图片高度
                .thumbnailWidth(160)// 选择界面图片宽度
                .enableClickSound(false)// 是否开启点击声音
                .pickedMediaList(mMediaAdapter.getData())// 已选图片数据
                .videoFilterTime(360)//显示多少秒以内的视频
                .mediaFilterSize(10000)//显示多少kb以下的图片/视频，默认为0，表示不限制
                .start(this, PhoenixOption.TYPE_PICK_MEDIA, REQUEST_CODE);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_upload:
                if (!isNetworkConnected())
                {
                    showToast("No connection");
                    return;
                }
                if (ftp == null)
                    initFtp();
                List<MediaEntity> data = mMediaAdapter.getData();
                String [] filePaths = new String[data.size()];
                for (int i = 0; i < filePaths.length; i++)
                    filePaths[i] = data.get(i).getLocalPath();
                upload(filePaths);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //返回的数据
            List<MediaEntity> result = Phoenix.result(data);
            for(MediaEntity e: result)
            {
                Log.d("MediaEntity", e.getLocalPath());
            }
            mMediaAdapter.setData(result);
        }
    }

    //上传
    private void upload(String[] localFilePaths)
    {
        new FtpUploadTask(ftp, ((MainActivity) getActivity()).getFragments(), localFilePaths, currentFtpPath, true).execute();
    }

    public void uploadFinish(Integer result)
    {
        if (result > 0)
        {
            showToast("Upload " + result + " files successfully");
            // 上传成功，清空RecyclerView
            mMediaAdapter.removeAllItem();
        } else
            {
            showToast("Upload an empty folder or fail");
        }
    }

    private boolean isNetworkConnected()
    {
        return ((MainActivity) getActivity()).isNetworkConnected();
    }

    private void showToast(String string)
    {
        Toast.makeText(getActivity().getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }
}


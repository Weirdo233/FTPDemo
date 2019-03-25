package com.alfredwei.ftpdemo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.util.List;
import com.bumptech.glide.Glide;

import org.apache.commons.net.ftp.FTPFile;

public class FtpFileCheckAdapter extends ArrayAdapter<FTPFile>
{
    // 每个item的id
    private int itemId;

    public FtpFileCheckAdapter(Context context, int resource, List<FTPFile> objects)
    {
        super(context, resource, objects);
        itemId = resource;
    }

    // 获取position位置的item的view
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FTPFile file = getItem(position);
        View view;
        CheckViewHolder viewHolder;
        if (convertView == null)
        {
            view = LayoutInflater.from(getContext()).inflate(itemId, parent, false);
            viewHolder = new CheckViewHolder();
            viewHolder.icon = view.findViewById(R.id.file_image);
            viewHolder.checkedTextView = view.findViewById(R.id.checkedTextView);
            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (CheckViewHolder) view.getTag();
        }

        viewHolder.checkedTextView.setText(file.getName());
        String fileType =  file.getName().substring(file.getName().lastIndexOf(".") + 1);
        if (file.isDirectory())
            viewHolder.icon.setImageResource(R.drawable.folder_2);
        else
            viewHolder.icon.setImageResource(R.drawable.file);
        Log.d(getContext().getPackageName(), file.getName());
        return view;
    }
}

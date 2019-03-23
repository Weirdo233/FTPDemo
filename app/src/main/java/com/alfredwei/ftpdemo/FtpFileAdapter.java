package com.alfredwei.ftpdemo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alfredwei.ftpdemo.ItemViewHolder;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.util.List;

public class FtpFileAdapter extends ArrayAdapter<FTPFile>
{
    // 每个item_layout的id
    private int itemId;

    public FtpFileAdapter(Context context, int resource, List<FTPFile> objects)
    {
        super(context, resource, objects);
        itemId = resource;
    }

    // 获取position位置的item的view
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FTPFile file = getItem(position);
        View view;
        ItemViewHolder viewHolder;
        if (convertView == null)
        {
            view = LayoutInflater.from(getContext()).inflate(itemId, parent, false);
            viewHolder = new ItemViewHolder();
            viewHolder.fileName = (TextView) view.findViewById(R.id.file_name);
            viewHolder.icon = (ImageView) view.findViewById(R.id.file_image);

            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ItemViewHolder) view.getTag();
        }

        viewHolder.fileName.setText(file.getName());
        if (file.isDirectory())
            viewHolder.icon.setImageResource(R.drawable.folder_2);
        else
            viewHolder.icon.setImageResource(R.drawable.file);
        Log.d(getContext().getPackageName(), file.getName());
        return view;
    }
}

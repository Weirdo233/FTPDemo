package com.alfredwei.ftpdemo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.alfredwei.ftpdemo.ItemViewHolder;
import java.io.File;
import java.util.List;
import com.bumptech.glide.Glide;
import com.bumptech.glide.*;
import com.bumptech.glide.request.target.SimpleTarget;

public class LocalFileAdapter extends ArrayAdapter<File>
{
    // 每个item的id
    private int itemId;

    public LocalFileAdapter(Context context, int resource, List<File> objects)
    {
        super(context, resource, objects);
        itemId = resource;
    }

    // 获取position位置的item的view
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        File file = getItem(position);
        View view;
        final ItemViewHolder viewHolder;
        if (convertView == null)
        {
            view = LayoutInflater.from(getContext()).inflate(itemId, parent, false);
            viewHolder = new ItemViewHolder();
            viewHolder.fileName = view.findViewById(R.id.file_name);
            viewHolder.icon = view.findViewById(R.id.file_image);
            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ItemViewHolder) view.getTag();
        }

        viewHolder.fileName.setText(file.getName());
        String fileType =  file.getName().substring(file.getName().lastIndexOf(".") + 1);
        if (file.isDirectory())
            viewHolder.icon.setImageResource(R.drawable.folder_2);
        // 加载缩略图
        else if (fileType.equals("jpg") || fileType.equals("jpeg") || fileType.equals("png"))
            Glide.with(view)
                    .load(file.getAbsolutePath())
                    .thumbnail(0.1f)
                    .into(viewHolder.icon);
        else
            viewHolder.icon.setImageResource(R.drawable.file);



        Log.d(getContext().getPackageName(), file.getName());
        return view;
    }
}

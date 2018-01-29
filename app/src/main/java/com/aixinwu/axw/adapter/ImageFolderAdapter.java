package com.aixinwu.axw.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.model.FolderInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lionel on 2018/1/17.
 */

public class ImageFolderAdapter extends BaseAdapter{

    private LayoutInflater inflater;
    private List<FolderInfo> folders;
    private int selected = 0;
    private int color;

    public ImageFolderAdapter(Context context, List<FolderInfo> list) {
        inflater = LayoutInflater.from(context);
        folders = new ArrayList<>(list);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        color = context.getResources().getColor(typedValue.resourceId);
        color = (color & 0x00000000)| (color & 0x90FFFFFF);
    }

    @Override
    public int getCount() {
        return folders.size();
    }

    @Override
    public FolderInfo getItem(int i) {
        return folders.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setSelected(int i){
        selected = i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        VHolder holder=null;
        if (view == null) {
            holder = new VHolder();
            view = inflater.inflate(R.layout.item_photo_folder, null);
            if(i == selected) view.setBackgroundColor(color);
            holder.imageView = (ImageView) view.findViewById(R.id.folder_img);
            holder.name = (TextView) view.findViewById(R.id.folder_name);
            holder.num = (TextView) view.findViewById(R.id.folder_num);
            view.setTag(holder);
        }else
            holder = (VHolder) view.getTag();
        holder.name.setText(folders.get(i).getName());
        if(folders.get(i).getPhotoInfoList() != null){
            int size = folders.get(i).getPhotoInfoList().size();
            holder.num.setText(Integer.toString(size)+"张");
            String path = "file://"+ folders.get(i).getPhotoInfoList().get(0).getPath();
            ImageLoader.getInstance().displayImage(path, holder.imageView);
        }
        return view;
    }

    public class VHolder{
        ImageView imageView;
        TextView name;
        TextView num;
    }
}

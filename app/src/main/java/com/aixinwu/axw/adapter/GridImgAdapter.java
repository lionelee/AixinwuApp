package com.aixinwu.axw.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aixinwu.axw.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by lionel on 2018/1/19.
 */

public class GridImgAdapter extends BaseAdapter{

    private LayoutInflater inflater;
    private ArrayList<String> images;
    private OnClickListener listener;

    public GridImgAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        images = new ArrayList<>();
        images.add("");
    }

    public void setList(ArrayList<String> list){
        images.clear();
        images.addAll(list);
        if(images.size() < 8){
            images.add("");
        }
    }

    public void setListener(OnClickListener listener){
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public String getItem(int i) {
        return images.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        String path = images.get(i);
        final int idx = i;
        if(path.equals("")){
//            if(view == null) {
                view = inflater.inflate(R.layout.item_grid_addpic, null);
                AddPicVHolder holder = new AddPicVHolder();
                holder.iv_pic = (ImageView) view.findViewById(R.id.iv_pic);
                holder.iv_pic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onClick(view, idx);
                    }
                });
//                view.setTag(holder);
//            }
        }else{
            PicVHolder holder;
//            if(view == null) {
                view = inflater.inflate(R.layout.item_grid_img, null);
                holder = new PicVHolder();
                holder.iv_pic = (ImageView) view.findViewById(R.id.iv_pic);
                holder.iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
                holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        images.remove(idx);
                        listener.onClick(view, idx);
                    }
                });
//                view.setTag(holder);
//            } else {
//                holder = (PicVHolder) view.getTag();
//            }
            ImageLoader.getInstance().displayImage("file://"+path, holder.iv_pic);
        }
        return view;
    }

    private class PicVHolder{
        private ImageView iv_pic;
        private ImageView iv_delete;
    }

    public class AddPicVHolder{
        private ImageView iv_pic;
    }

    public interface OnClickListener{
        public void onClick(View view, int i);
    }
}

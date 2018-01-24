package com.aixinwu.axw.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.activity.PhotoPicker;
import com.aixinwu.axw.model.PhotoInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lionel on 2018/1/18.
 */

public class PhotoPickAdapter extends RecyclerView.Adapter<ViewHolder>{

    private static final int ITEM_TYPE_CAMERA = 0;
    private static final int ITEM_TYPE_NORMAL = 1;

    private LayoutInflater inflater;
    private int mode = PhotoPicker.SINGLE_SELECT;
    private boolean flag = false;
    private List<PhotoInfo> photos = new ArrayList<>();
    private List<String> selects;
    private OnCheckedChangeListener listener;

    public PhotoPickAdapter(Context context, int m) {
        inflater = LayoutInflater.from(context);
        mode = m;
        if(mode == PhotoPicker.MULTI_SELECT){
            selects = new ArrayList<>();
        }
    }

    public void setList(List<PhotoInfo> list){
        photos.clear();
        photos.addAll(list);
    }

    public void setSelects(List<String> list){
        selects.addAll(list);
    }

    public void setListener(OnCheckedChangeListener listener){
        this.listener = listener;
    }

    public void setFlag(Boolean flag){
        this.flag = flag;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ITEM_TYPE_CAMERA){
            return new CamVHolder(inflater.inflate(R.layout.item_camera, null));
        }else{
            return new ImgVHolder(inflater.inflate(R.layout.item_photo_list, null));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof ImgVHolder){
            ((ImgVHolder)holder).bindData(photos.get(position-1).getPath());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? ITEM_TYPE_CAMERA : ITEM_TYPE_NORMAL;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return photos.size() + 1;
    }

    public void clear(){
        photos.clear();
    }

    public class ImgVHolder extends ViewHolder{
        String path;
        ImageView img;
        CheckBox cb;
        View mask;

        public ImgVHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.item_photo_img);
            cb = (CheckBox) itemView.findViewById(R.id.item_photo_cb);
            mask = itemView.findViewById(R.id.mask);
        }

        public void bindData(String _path){
            this.path = _path;
            ImageLoader.getInstance().displayImage("file://"+path, img);
            if(mode == PhotoPicker.MULTI_SELECT){
                cb.setVisibility(View.VISIBLE);
                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b){
                            if(!selects.contains(path)){
                                selects.add(path);
                                listener.onCheckedChanged(b, path);
                            }
                        }
                        else {
                            if(selects.contains(path)){
                                selects.remove(path);
                                listener.onCheckedChanged(b, path);
                            }
                        }
                    }
                });
                if(selects.contains(path)){
                    mask.setVisibility(View.GONE);
                    cb.setClickable(true);
                    cb.setChecked(true);
                    return;
                }else{
                    cb.setChecked(false);
                }
                if(flag){
                    mask.setVisibility(View.VISIBLE);
                    cb.setClickable(false);
                }else{
                    mask.setVisibility(View.GONE);
                    cb.setClickable(true);
                }
            }
        }

        public String getPath(){
            return path;
        }
    }

    public class CamVHolder extends ViewHolder{
        public CamVHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnCheckedChangeListener{
        public void onCheckedChanged(boolean b, String path);
    }
}

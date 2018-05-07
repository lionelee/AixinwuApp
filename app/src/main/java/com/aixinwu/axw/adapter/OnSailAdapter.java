package com.aixinwu.axw.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.activity.Buy;
import com.aixinwu.axw.model.Record;
import com.aixinwu.axw.tools.Bean;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangyuding on 2016/10/14.
 * Modified by lionel on 2018/1/22
 */
public class OnSailAdapter extends RecyclerView.Adapter<OnSailAdapter.ViewHolder> {

    private Context context;
    private Handler nHandler;
    private List<Bean>datas;

    public OnSailAdapter(Context context, Handler handler) {
        this.context = context;
        this.nHandler = handler;
        datas = new ArrayList<>();
    }

    public void setList(List<Bean> data){
        datas.clear();
        datas.addAll(data);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bean_list, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView title, sail, content;
        TextView onShelf, offShelf;

        public ViewHolder(View view){
            super(view);
            img = (ImageView) view.findViewById(R.id.item_search_iv_icon);
            title = (TextView) view.findViewById(R.id.item_search_tv_title);
            content = (TextView) view.findViewById(R.id.item_search_tv_content);
            sail = (TextView) view.findViewById(R.id.whether_on_sail);
            onShelf = (TextView) view.findViewById(R.id.onShelf);
            offShelf = (TextView) view.findViewById(R.id.offShelf);
        }

        public void bindData(final int position){
            Bean item = datas.get(position);
            title.setText(item.getType());
            content.setText(item.getDoc());
            sail.setText(item.getWhetherOnSail());
            ImageLoader.getInstance().displayImage(item.getPicId(),img);
            if(item.getOnOrNot() == 1) {
                onShelf.setVisibility(View.GONE);
                offShelf.setVisibility(View.VISIBLE);
            }else{
                onShelf.setVisibility(View.VISIBLE);
                offShelf.setVisibility(View.GONE);
            }
            offShelf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(context)
                            .setTitle("提示")
                            .setMessage("是否下架该商品？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int ii) {
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            Message msg = new Message();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("position",""+position);  //往Bundle中存放数据
                                            bundle.putString("whetherOn",""+2);
                                            msg.setData(bundle);//mes利用Bundle传递数据
                                            msg.what = 1322;
                                            nHandler.sendMessage(msg);

                                        }
                                    }.start();

                                }
                            })
                            .setNegativeButton("否", null)
                            .show();
                }
            });

            onShelf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(context)
                            .setTitle("提示")
                            .setMessage("是否上架该商品？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int ii) {
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            Message msg = new Message();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("position",""+position);  //往Bundle中存放数据
                                            bundle.putString("whetherOn",""+0);
                                            msg.setData(bundle);//mes利用Bundle传递数据
                                            msg.what = 1322;
                                            nHandler.sendMessage(msg);

                                        }
                                    }.start();

                                }
                            })
                            .setNegativeButton("否", null)
                            .show();
                }
            });
        }

        public Bean getData(){
            int pos = this.getAdapterPosition();
            return datas.get(pos);
        }
    }
}



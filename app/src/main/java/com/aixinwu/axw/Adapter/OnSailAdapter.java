package com.aixinwu.axw.Adapter;

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

import com.aixinwu.axw.R;
import com.aixinwu.axw.activity.Buy;
import com.aixinwu.axw.tools.Bean;
import com.aixinwu.axw.tools.CommonAdapter;
import com.aixinwu.axw.tools.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangyuding on 2016/10/14.
 * Modified by lionel on 2017/10/31
 */
public class OnSailAdapter extends RecyclerView.Adapter<OnSailAdapter.ViewHolder>{

    private Context context;
    private Handler nHandler;
    LayoutInflater inflater;
    private List<Bean> beans = new ArrayList<>();

    public OnSailAdapter(Context context, List<Bean> data) {
        this.inflater = LayoutInflater.from(context);
        this.beans = data;
    }

    @Override
    public void convert(ViewHolder holder, final int position) {
        holder.setImageBitmap(R.id.item_search_iv_icon,mData.get(position).getPicId())
                .setText(R.id.item_search_tv_title,mData.get(position).getType())
                .setText(R.id.item_search_tv_content,mData.get(position).getDoc())
                .setText(R.id.whether_on_sail, mData.get(position).getWhetherOnSail());
        if(mData.get(position).getOnOrNot() == 1){
            holder.getView(R.id.onShelf).setVisibility(View.GONE);
            holder.getView(R.id.offShelf).setVisibility(View.VISIBLE);
        }
        else if (mData.get(position).getOnOrNot() == 0){
            holder.getView(R.id.onShelf).setVisibility(View.VISIBLE);
            holder.getView(R.id.offShelf).setVisibility(View.GONE);
        }

        holder.getView(R.id.item_bean_list_content_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("itemId", mData.get(position).getItemId());
                intent.putExtra("caption",mData.get(position).getType());
                intent.setClass(context, Buy.class);
                context.startActivity(intent);
            }
        });

        holder.getView(R.id.offShelf).setOnClickListener(new View.OnClickListener() {
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
                                        super.run();
                                        //changestatus(upData.get(now).getItemId(),2);
                                        //getDbData();
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

        holder.getView(R.id.onShelf).setOnClickListener(new View.OnClickListener() {
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
                                        super.run();
                                        //changestatus(downData.get(noww).getItemId(),0);
                                        Message msg = new Message();
                                        //getDbData();
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return beans.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{



        public ViewHolder(View view){
            super(view);
        }

        public void bindData(Bean bean){
        }

        public Bean getData(){
            int pos = this.getAdapterPosition();
            return beanList.get(pos);
        }

    }
}



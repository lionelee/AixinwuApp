package com.aixinwu.axw.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.tools.Bean;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnItemLongClick;

/**
 * Created by lionel on 2017/10/15.
 */

public class DealListAdapter extends RecyclerView.Adapter<DealListAdapter.VHolder>{
    LayoutInflater inflater;
    List<Bean> beanList;

    public DealListAdapter(Context context){
        inflater = LayoutInflater.from(context);
        beanList = new ArrayList<>();
    }

    public void addItem(Bean bean){
        beanList.add(bean);
        notifyItemInserted(beanList.size());
    }

    public void clear(){
        notifyItemRangeRemoved(0,beanList.size());
        beanList.clear();
    }

    @Override
    public VHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_used_commodity, parent, false);
        return new VHolder(view);
    }

    @Override
    public void onBindViewHolder(VHolder holder, int position) {
        holder.bindData(beanList.get(position));
    }

    @Override
    public int getItemCount() {
        return beanList.size();
    }

    public class VHolder extends RecyclerView.ViewHolder{
        ImageView iv_img;
        TextView tv_name;
        TextView tv_price;

        public VHolder(View itemView) {
            super(itemView);
            iv_img = (ImageView) itemView.findViewById(R.id.item_used_img);
            tv_name = (TextView) itemView.findViewById(R.id.item_used_name);
            tv_price = (TextView) itemView.findViewById(R.id.item_used_price);
        }

        public void bindData(Bean item){
            ImageLoader.getInstance().displayImage(item.getPicId(),iv_img);
            tv_name.setText(item.getType());
            tv_price.setText("¥"+item.getPrice());
        }

        public Bean getData(){
            int pos = this.getAdapterPosition();
            return beanList.get(pos);
        }
    }
}

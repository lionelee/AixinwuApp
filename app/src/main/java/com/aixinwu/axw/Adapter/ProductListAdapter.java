package com.aixinwu.axw.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aixinwu.axw.R;
//import com.jcodecraeer.xrecyclerview.ItemTouchHelperAdapter;


import java.util.ArrayList;
import java.util.List;

import com.aixinwu.axw.model.Product;
import com.marshalchen.ultimaterecyclerview.SwipeableUltimateViewAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {
//    extends UltimateViewAdapter<ProductListAdapter.ViewHolder>{
    LayoutInflater inflater;
    private List<Product> datas = null;

    public ProductListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        datas = new ArrayList<>();
    }

    public void addItem(Product product){
        datas.add(product);
        notifyItemInserted(datas.size());
    }

    public void clear(){
        notifyItemRangeRemoved(0,datas.size());
        datas.clear();
    }

//    @Override
//    public ViewHolder newFooterHolder(View view) {
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public ViewHolder newHeaderHolder(View view) {
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent) {
//        View view = inflater.inflate(R.layout.activity_list_product, null);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public int getAdapterItemCount() {
//        return datas.size();
//    }
//
//    @Override
//    public long generateHeaderId(int position) {
//        return -1;
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
//        View view = inflater.inflate(R.layout.activity_list_product, null);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
//        ((ViewHolder)holder).mNameView.setText("");
//        ((ViewHolder)holder).mPriceView.setText("");
//        ((ViewHolder)holder).mImageView.setImageResource(R.drawable.icon);
//    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = inflater.inflate(R.layout.activity_list_product, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
//        if(position >= getItemCount())return;
//        if(customHeaderView == null){
//            if(position >= datas.size())return;
//        }else{
//            if(position > datas.size() || position <= 0)return;
//            position -= 1;
//        }
        viewHolder.mNameView.setText(datas.get(position).getProduct_name());
        viewHolder.mPriceView.setText(datas.get(position).getPrice() + "");
        ImageLoader.getInstance().displayImage(datas.get(position).getImage_url(), viewHolder.mImageView);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mNameView;
        public TextView mPriceView;
        public ViewHolder(View view){
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.product_image);
            mNameView = (TextView) view.findViewById(R.id.product_name);
            mPriceView = (TextView) view.findViewById(R.id.product_price);
        }

        public Product getData(){
            int pos = this.getAdapterPosition();
            return datas.get(pos);
        }
    }
}

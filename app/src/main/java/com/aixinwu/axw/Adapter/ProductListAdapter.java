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
import com.nostra13.universalimageloader.core.ImageLoader;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {
    LayoutInflater inflater;
    private List<Product> datas = null;
    int mode = 0;

    public ProductListAdapter(Context context, int mode) {
        inflater = LayoutInflater.from(context);
        datas = new ArrayList<>();
        this.mode = mode;
    }

    public void addItem(Product product){
        datas.add(product);
        notifyItemInserted(datas.size());
    }

    public void clear(){
        notifyItemRangeRemoved(0,datas.size());
        datas.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        if(viewType == 0) view = inflater.inflate(R.layout.item_product_listd, null);
        else view = inflater.inflate(R.layout.item_product_lists, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.mNameView.setText(datas.get(position).getProduct_name());
        viewHolder.mPriceView.setText(datas.get(position).getPrice() + "");
        ImageLoader.getInstance().displayImage(datas.get(position).getImage_url(), viewHolder.mImageView);
    }

    @Override
    public int getItemViewType(int position) {
        return mode;
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

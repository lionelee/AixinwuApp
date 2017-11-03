package com.aixinwu.axw.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.model.Product;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by dell1 on 2016/4/24.
 */
public class ProductAdapter extends BaseAdapter {
    private int resourceId;
    public Product product;
    private List<Product> products;
    private Context mContext;

    public ProductAdapter (Context context, int textViewResourseId, List<Product> objects) {
        mContext = context;
        resourceId = textViewResourseId;
        int size = objects.size();
        if(size>6) size = 6;
        products = new ArrayList<>();
        for(int i = 0; i < size; ++i){
            products.add(objects.get(i));
        }
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Product getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View concertView, ViewGroup parent) {
        product = getItem(position);
        VHolder holder;
        if(concertView == null) {
            concertView = LayoutInflater.from(mContext).inflate(resourceId, null);
            holder = new VHolder();
            holder.imageView = (ImageView)concertView.findViewById(R.id.product_image);
            holder.name = (TextView)concertView.findViewById(R.id.product_name);
            holder.price = (TextView)concertView.findViewById(R.id.product_price);
            concertView.setTag(holder);
        } else {
            holder = (VHolder) concertView.getTag();
        }
        ImageLoader.getInstance().displayImage(product.getImage_url(), holder.imageView);
        holder.name.setText(product.getProduct_name());
        holder.price.setText(String.valueOf(product.getPrice()));
        return concertView;

    }

    private class VHolder{
        ImageView imageView;
        TextView name;
        TextView price;
    }

}

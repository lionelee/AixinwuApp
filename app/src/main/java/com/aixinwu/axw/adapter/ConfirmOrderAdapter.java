package com.aixinwu.axw.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.model.ShoppingCartEntity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by liangyuding on 2016/10/16.
 */
public class ConfirmOrderAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<ShoppingCartEntity> mDatas = new ArrayList<>();
    private ViewHolder holder;


    public ConfirmOrderAdapter(Context context, ArrayList<ShoppingCartEntity> mDatas) {
        mInflater = LayoutInflater.from(context);
        this.mDatas = mDatas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_confirm_order, parent, false);
            holder.cb = (CheckBox) convertView.findViewById(R.id.cb_item_shopping_cart);
            holder.name = (TextView) convertView.findViewById(R.id.tv_item_shopping_cart_name);
            holder.category = (TextView) convertView.findViewById(R.id
                    .tv_item_shopping_cart_category);
            holder.price = (TextView) convertView.findViewById(R.id
                    .tv_item_shopping_cart_price);
            holder.number = (TextView) convertView.findViewById(R.id
                    .tv_item_shopping_cart_number);
            holder.img = (ImageView) convertView.findViewById(R.id
                    .img_item_shopping_cart_number);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ShoppingCartEntity entity = (ShoppingCartEntity) getItem(position);
        holder.category.setText(entity.getCategory());
        holder.name.setText(entity.getName());
        holder.price.setText(entity.getPrice() + "");
        holder.number.setText("x" + entity.getNumber());
        ImageLoader.getInstance().displayImage(entity.getImgUrl(), holder.img);


        return convertView;
    }

    class ViewHolder {
        CheckBox cb;
        ImageView img;
        TextView name, category, price, number;
    }
}

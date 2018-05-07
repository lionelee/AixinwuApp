package com.aixinwu.axw.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.model.VolunteerActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hello on 2016/10/29.
 */
public class VolunteerAdapter extends BaseAdapter {
    private int resourceId;
    private  List<VolunteerActivity> products = new ArrayList<>();
    private Context mContext;

    public VolunteerAdapter (Context context, int textViewResourseId) {
        mContext = context;
        resourceId = textViewResourseId;
    }

    public void resetList(List<VolunteerActivity> objects){
        products.clear();
        int size = objects.size();
        for(int i = 0; i < size; ++i){
            products.add(objects.get(i));
        }
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public VolunteerActivity getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View concertView, ViewGroup parent) {
        VolunteerActivity product = getItem(position);
        VHolder holder;
        if(concertView == null) {
            concertView = LayoutInflater.from(mContext).inflate(resourceId, null);
            holder = new VHolder();
            holder.imageView = (ImageView)concertView.findViewById(R.id.product_image);
            holder.name = (TextView)concertView.findViewById(R.id.product_name);
            holder.price = (TextView)concertView.findViewById(R.id.product_price);
            holder.workDate = (TextView) concertView.findViewById(R.id.workDate);
            holder.fullOrNot = (TextView) concertView.findViewById(R.id.fullOrNot);
            concertView.setTag(holder);
        } else {
            holder = (VHolder) concertView.getTag();
        }

        int need = product.getNeededPeople();
        int signed = product.getSignedPeople();

        if (need <= signed && need != 0)
            holder.fullOrNot.setText("名额已满   " + signed+"/"+need);
        else{
            if (need == 0)
                holder.fullOrNot.setText("欢迎报名   " + signed+"/∞");
            else if (need > signed)
                holder.fullOrNot.setText("欢迎报名   " + signed+"/"+need);
        }

        if (!product.getImg_url().equals(""))
            ImageLoader.getInstance().displayImage(product.getImg_url(), holder.imageView);
        holder.name.setText(product.getName());
        holder.price.setText("爱心币：+"+String.valueOf(product.getPayback()));
        holder.workDate.setText("时间："+product.getTime().substring(5,10)+" "+product.getTime().substring(11,16));
        return concertView;
    }

    private class VHolder{
        ImageView imageView;
        TextView name;
        TextView price;
        TextView workDate;
        TextView fullOrNot;
    }
}

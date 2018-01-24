package com.aixinwu.axw.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.model.VolunteerActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lionel on 2018/1/16.
 */

public class VolunteerListAdapter extends RecyclerView.Adapter<VolunteerListAdapter.ViewHolder> {
    LayoutInflater inflater;
    private List<VolunteerActivity> datas = new ArrayList<>();

    public VolunteerListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        datas = new ArrayList<>();
    }

    public void addItem(VolunteerActivity va){
        datas.add(va);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = inflater.inflate(R.layout.volunteer_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        VolunteerActivity product = datas.get(position);
        int need = product.getNeededPeople();
        int signed = product.getSignedPeople();

        if (need <= signed && need != 0)
            viewHolder.fullOrNot.setText("名额已满   " + signed+"/"+need);
        else{
            if (need == 0)
                viewHolder.fullOrNot.setText("欢迎报名   " + signed+"/∞");
            else if (need > signed)
                viewHolder.fullOrNot.setText("欢迎报名   " + signed+"/"+need);
        }

        if (!product.getImg_url().equals(""))
            ImageLoader.getInstance().displayImage(product.getImg_url(), viewHolder.imageView);
        viewHolder.name.setText(product.getName());
        viewHolder.price.setText("爱心币：+"+String.valueOf(product.getPayback()));
        viewHolder.workDate.setText("时间："+product.getTime().substring(5,10)+" "+product.getTime().substring(11,16));
     }

    @Override
    public int getItemCount() {
        return datas.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name;
        TextView price;
        TextView workDate;
        TextView fullOrNot;

        public ViewHolder(View view){
            super(view);
            imageView = (ImageView) view.findViewById(R.id.product_image);
            name = (TextView) view.findViewById(R.id.product_name);
            price = (TextView) view.findViewById(R.id.product_price);
            workDate = (TextView) view.findViewById(R.id.workDate);
            fullOrNot = (TextView) view.findViewById(R.id.fullOrNot);
        }

        public VolunteerActivity getData(){
            int pos = this.getAdapterPosition();
            return datas.get(pos);
        }
    }
}
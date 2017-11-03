package com.aixinwu.axw.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.model.Record;
import com.aixinwu.axw.model.ShoppingCartEntity;
import com.aixinwu.axw.tools.Bean;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangyuding on 2016/10/29.
 * Modified by lionel on 2017/10/31
 */
public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    LayoutInflater inflater;
    private ArrayList<Record> mDatas = new ArrayList<>();


    public RecordAdapter(Context context, ArrayList<Record> mDatas) {
        this.inflater = LayoutInflater.from(context);
        this.mDatas = mDatas;
    }

    @Override
    public int getItemCount() { return mDatas.size(); }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(mDatas.get(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView name, price, number;

        public ViewHolder(View view){
            super(view);
            img = (ImageView) view.findViewById(R.id.img);
            name = (TextView) view.findViewById(R.id.tv_item_shopping_cart_name);
            price = (TextView) view.findViewById(R.id.tv_item_shopping_cart_price);
            number = (TextView) view.findViewById(R.id.tv_item_shopping_cart_number);
        }

        public void bindData(Record item){
            name.setText("订单编号："+ item.getOrder_sn());
            price.setText(item.getTotal_product_price());
            number.setText(item.getUpdateTime());
            String [] imgUrls = item.getImgUrls().split(",");

            if(imgUrls.length > 0)
                ImageLoader.getInstance().displayImage(GlobalParameterApplication.axwUrl+imgUrls[0], img);
        }

        public Record getData(){
            int pos = this.getAdapterPosition();
            return mDatas.get(pos);
        }

    }
}

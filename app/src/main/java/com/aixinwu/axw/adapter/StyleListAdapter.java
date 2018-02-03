package com.aixinwu.axw.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aixinwu.axw.R;

/**
 * Created by lionel on 2018/1/29.
 */

public class StyleListAdapter extends BaseAdapter {

    private String names[];
    private Context mContext;
    private LayoutInflater inflater;
    private int select;
    int colors[] ={R.color.primary, R.color.SeieeBlue, R.color.UltraViolet, R.color.ChiliOil,
            R.color.LittleBoyBlue, R.color.Arcadia, R.color.Emperador};

    public StyleListAdapter(Context context, int s){
        mContext = context;
        inflater = LayoutInflater.from(context);
        names =  context.getResources().getStringArray(R.array.pref_theme_option_labels);
        select = s;
    }

    public void chgSelect(int s){
        select = s;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        VHolder vHolder;
        if(view == null){
            view = inflater.inflate(R.layout.item_style_list,null);
            vHolder = new VHolder();
            vHolder.iv_color = (ImageView) view.findViewById(R.id.style_color);
            vHolder.tv_name = (TextView) view.findViewById(R.id.style_name);
            view.setTag(vHolder);
        }else
            vHolder = (VHolder)view.getTag();
        vHolder.iv_color.setBackgroundResource(colors[i]);
        vHolder.iv_color.setImageDrawable(null);
        vHolder.tv_name.setText(names[i]);
        vHolder.tv_name.setTextColor(mContext.getResources().getColor(colors[i]));
        if(i == select) vHolder.iv_color.setImageResource(R.drawable.checkmark);
        return view;
    }

    private class VHolder{
        private ImageView iv_color;
        private TextView tv_name;
    }
}

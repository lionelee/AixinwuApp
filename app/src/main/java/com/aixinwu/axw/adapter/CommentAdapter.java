package com.aixinwu.axw.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aixinwu.axw.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by lionel on 2018/1/24.
 */

public class CommentAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<HashMap<String, String>> comment_list = new ArrayList<>();

    public CommentAdapter(Context context, ArrayList<HashMap<String, String>> list){
        inflater = LayoutInflater.from(context);
        comment_list.addAll(list);
    }

    public void addItem(String comm, String time){
        HashMap<String, String> map = new HashMap<>();
        map.put("comment", comm);
        map.put("time", time);
        comment_list.add(map);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return comment_list.size();
    }

    @Override
    public Object getItem(int i) {
        return comment_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        CommentVHolder holder=null;
        if (view == null) {
            holder = new CommentVHolder();
            view = inflater.inflate(R.layout.item_comment, null);
            holder.iv_avatar = (CircleImageView) view.findViewById(R.id.comment_avatar);
            holder.tv_comment = (TextView) view.findViewById(R.id.comment_text);
            holder.tv_time = (TextView) view.findViewById(R.id.comment_time);
            view.setTag(holder);
        }else
            holder = (CommentVHolder) view.getTag();
        holder.iv_avatar.setImageResource(R.drawable.scrollview_header);
        holder.tv_comment.setText(comment_list.get(i).get("comment"));
        holder.tv_time.setText(comment_list.get(i).get("time"));
        return view;
    }

    private class CommentVHolder {
        CircleImageView iv_avatar;
        TextView tv_comment;
        TextView tv_time;
    }
}

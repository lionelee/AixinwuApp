package com.aixinwu.axw.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aixinwu.axw.R;
import com.aixinwu.axw.activity.Buy;
import com.aixinwu.axw.database.Sqlite;
import com.aixinwu.axw.tools.Bean;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.widget.RecyclerViewDivider;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class MyCollection extends Fragment {


    private RecyclerView collectionList;
    private List<Bean> collectList = new ArrayList<>();
    private MyCollectionAdapter myCollectionAdapter;

    private Sqlite userDbHelper;

    private Handler dHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 521521:
                    myCollectionAdapter = new MyCollectionAdapter(getActivity(),collectList);
                    collectionList.setAdapter(myCollectionAdapter);
                    break;
            }
        };
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_my_collection,null);
        collectionList = (RecyclerView) view.findViewById(R.id.collectionList);
        collectionList.setLayoutManager(new LinearLayoutManager(getActivity()));
        collectionList.addItemDecoration(new RecyclerViewDivider(getActivity()));

        userDbHelper = new Sqlite(getActivity());

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = userDbHelper.getWritableDatabase();
                Cursor cursor = db.rawQuery("select itemId,picUrl,type,price from AXWcollect where userName = '" + GlobalParameterApplication.getUser_name() + "'", null);
                while (cursor.moveToNext()) {

                    collectList.add(new Bean(cursor.getInt(0), cursor.getString(1), cursor.getString(2), "价格：" + (cursor.getInt(3))));

                }
                cursor.close();
                db.close();
                Message msg=new Message();
                msg.what=521521;
                dHandler.sendMessage(msg);
            }
        }).start();
        return view;
    }


    private class MyCollectionAdapter extends RecyclerView.Adapter<MyCollectionAdapter.ViewHolder> {

        private LayoutInflater mInflater;
        private List<Bean> mDatas = new ArrayList<>();


        public MyCollectionAdapter(Context context, List<Bean> mDatas) {
            mInflater = LayoutInflater.from(context);
            this.mDatas = mDatas;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_collection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindData(mDatas.get(position), position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            ImageView img;
            TextView name, price;
            RelativeLayout seeDetail,delCollection;

            public ViewHolder(View view){
                super(view);
                img = (ImageView) view.findViewById(R.id.collectImg);
                name = (TextView) view.findViewById(R.id.commodity_name);
                price = (TextView) view.findViewById(R.id.commodity_price);
                seeDetail = (RelativeLayout) view.findViewById(R.id.seeDetail);
                delCollection = (RelativeLayout) view.findViewById(R.id.collectComodity);
            }

            public void bindData(final Bean entity, final int position){
                name.setText(entity.getType());
                price.setText(entity.getDoc() + "");
                ImageLoader.getInstance().displayImage(entity.getPicId().trim(), img);

                seeDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.putExtra("itemId", entity.getItemId());
                        intent.putExtra("caption", entity.getType());
                        intent.setClass(MyCollection.this.getActivity(), Buy.class);
                        startActivity(intent);
                    }
                });

                delCollection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SQLiteDatabase db = userDbHelper.getWritableDatabase();
                        db.execSQL("delete from AXWcollect where itemId ="+entity.getItemId()+" and userName = '" + GlobalParameterApplication.getUser_name() + "'");
                        mDatas.remove(entity);
                        myCollectionAdapter.notifyItemRemoved(position);
                        db.close();
                    }
                });

            }
        }
    }

}

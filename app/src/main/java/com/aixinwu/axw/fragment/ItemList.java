package com.aixinwu.axw.fragment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
//import android.support.v7.internal.widget.ThemeUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aixinwu.axw.activity.Buy;
import com.aixinwu.axw.adapter.OnSailAdapter;
import com.aixinwu.axw.R;
import com.aixinwu.axw.tools.Bean;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.OnRecyclerItemClickListener;
import com.aixinwu.axw.widget.RecyclerViewDivider;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ItemList extends Fragment {
    private String MyToken;
    private static List<Bean> upData=new ArrayList<Bean>();
    private OnSailAdapter upadapter;
    private RecyclerView uplist;
    private String surl = GlobalParameterApplication.getSurl();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_issue,null);
        uplist = (RecyclerView) view.findViewById(R.id.itemlistviewup);
        uplist.setLayoutManager(new LinearLayoutManager(getActivity()));
        uplist.addItemDecoration(new RecyclerViewDivider(getActivity()));
        upadapter = new OnSailAdapter(getActivity(), nHandler);
        uplist.setAdapter(upadapter);
        new Thread(new Runnable() {
            @Override
            public void run() {
                getDbData();
                Message msg = new Message();
                msg.what=1321;
                nHandler.sendMessage(msg);
            }
        }).start();
        uplist.addOnItemTouchListener(new OnRecyclerItemClickListener(uplist) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                Bean item = ((OnSailAdapter.ViewHolder)vh).getData();
                Intent intent = new Intent(getActivity(), Buy.class);
                intent.putExtra("itemId", item.getItemId());
                intent.putExtra("caption", item.getType());
                getActivity().startActivityForResult(intent, 0);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom,R.anim.scale_fade_out);
            }
        });
        return view;
    }

    public Handler nHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 1321:
                    upadapter.setList(upData);
                    upadapter.notifyDataSetChanged();
                    break;

            }
        }
    };

    private void getDbData(){
        MyToken= GlobalParameterApplication.getToken();
        JSONObject data = new JSONObject();
        data.put("token",MyToken);

        try {
            URL url = new URL(surl + "/item_get_list");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            conn.getOutputStream().write(data.toJSONString().getBytes());
            java.lang.String ostr = IOUtils.toString(conn.getInputStream());
            org.json.JSONObject outjson = new org.json.JSONObject(ostr);
            JSONArray result = outjson.getJSONArray("items");
            upData.clear();
            for (int i = result.length()-1; i >= 0 ; i--) {
                String[] rr = result.getJSONObject(i).getString("images").split(",");
                if (result.getJSONObject(i).getInt("status")==0) {
                    if (rr[0] == "") {
                        BitmapFactory.Options cc = new BitmapFactory.Options();
                        cc.inSampleSize = 20;
                        upData.add(new Bean(result.getJSONObject(i).getInt("ID"), "http://202.120.47.213:12345/img/1B4B907678CCD423", result.getJSONObject(i).getString("caption"), result.getJSONObject(i).getString("description"),"上架中",1));
                    } else
                        upData.add(new Bean(result.getJSONObject(i).getInt("ID"),GlobalParameterApplication.imgSurl + rr[0], result.getJSONObject(i).getString("caption"), result.getJSONObject(i).getString("description"),"上架中",1));
                } else{
                    if (rr[0] == "") {
                        BitmapFactory.Options cc = new BitmapFactory.Options();
                        cc.inSampleSize = 20;
                        upData.add(new Bean(result.getJSONObject(i).getInt("ID"), "http://202.120.47.213:12345/img/1B4B907678CCD423", result.getJSONObject(i).getString("caption"), result.getJSONObject(i).getString("description"),"已下架",0));
                    } else
                        upData.add(new Bean(result.getJSONObject(i).getInt("ID"), GlobalParameterApplication.imgSurl + rr[0], result.getJSONObject(i).getString("caption"), result.getJSONObject(i).getString("description"),"已下架",0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void changestatus(int id, int ch){
        MyToken= GlobalParameterApplication.getToken();
        JSONObject data = new JSONObject();
        JSONObject itemInfo = new JSONObject();
        itemInfo.put("ID",id);
        itemInfo.put("status",ch);
        data.put("itemInfo",itemInfo);
        data.put("token",MyToken);
        try {
            URL url = new URL(surl + "/item_set");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(data.toJSONString().getBytes());
            String ostr = IOUtils.toString(conn.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        org.json.JSONObject outjson = null;
        JSONArray result = null;
    }
}

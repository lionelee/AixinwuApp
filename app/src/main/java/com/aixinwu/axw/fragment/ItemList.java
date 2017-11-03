package com.aixinwu.axw.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.support.v7.internal.widget.ThemeUtils;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.aixinwu.axw.Adapter.OnSailAdapter;
import com.aixinwu.axw.R;
import com.aixinwu.axw.tools.Bean;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.SearchAdapter;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
        View view = inflater.inflate(R.layout.activity_item_list,null);
        uplist = (RecyclerView) view.findViewById(R.id.itemlistviewup);
        upadapter = new OnSailAdapter(getActivity(), upData, R.layout.item_bean_list,nHandler);
        uplist.setAdapter(upadapter);
        uplist.setVisibility(View.VISIBLE);
        mThread.start();
        return view;
    }

    public Thread mThread = new Thread(){
        @Override
        public void run(){
            super.run();
            getDbData();
            Message msg = new Message();
            msg.what=1321;
            nHandler.sendMessage(msg);
        }

    };

    public Handler nHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 1321:

                    int totalHeight = 0;
                    for (int i = 0; i < upadapter.getCount(); i++) {
                        View listItem = upadapter.getView(i, null, uplist);
                        listItem.measure(0, 0);
                        totalHeight += listItem.getMeasuredHeight();
                    }

                    ViewGroup.LayoutParams params = uplist.getLayoutParams();

                    upadapter.notifyDataSetChanged();
                    break;

            }
        }
    };

    private void getDbData(){
        MyToken= GlobalParameterApplication.getToken();
        JSONObject data = new JSONObject();
        data.put("token",MyToken);

        Log.i("UsedDeal", "get");
            try {
                URL url = new URL(surl + "/item_get_list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                conn.getOutputStream().write(data.toJSONString().getBytes());
                java.lang.String ostr = IOUtils.toString(conn.getInputStream());
                org.json.JSONObject outjson = null;
                JSONArray result = null;
                outjson = new org.json.JSONObject(ostr);
                result = outjson.getJSONArray("items");
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

}

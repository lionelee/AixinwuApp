package com.aixinwu.axw.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aixinwu.axw.activity.LaunchDialog;
import com.aixinwu.axw.activity.SendToAXW;
import com.aixinwu.axw.activity.SendToPeople;
import com.aixinwu.axw.adapter.DealListAdapter;
import com.aixinwu.axw.R;
import com.aixinwu.axw.activity.Buy;
import com.aixinwu.axw.activity.MainActivity;
import com.aixinwu.axw.tools.Bean;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.NetInfo;
import com.aixinwu.axw.tools.OnRecyclerItemClickListener;
import com.aixinwu.axw.view.MyFooterView;
import com.aixinwu.axw.view.MyRefreshLayout;
import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.melnykov.fab.FloatingActionButton;


import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangyuding on 2016/4/6.
 * Modified by lionel on 2017/10/16
 */
public class UsedDeal extends Fragment{

    private MyRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private int start = 0;
    private LaunchDialog dialog;

    public String MyToken;
    public String surl = GlobalParameterApplication.getSurl();

    private static DealListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.used_deal,null);
        mRefreshLayout = (MyRefreshLayout) view.findViewById(R.id.homepageScroll2);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                start = 0;
                new GetDataTask().execute(0);
            }
        });

        mRefreshLayout.setOnLoadListener(new MyRefreshLayout.OnLoadingListener() {
            @Override
            public void onLoad() {
                new GetDataTask().execute(1);
            }
        });
        mAdapter = new DealListAdapter(getActivity());
        mRecyclerView = (RecyclerView)view.findViewById(R.id.deal_item_list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new OnRecyclerItemClickListener(mRecyclerView){
        @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                if(vh instanceof DealListAdapter.VHolder){
                    Intent intent = new Intent();
                    Bean bean = ((DealListAdapter.VHolder)vh).getData();
                    intent.putExtra("itemId", bean.getItemId());
                    intent.putExtra("caption",bean.getType());
                    intent.putExtra("pic_url",bean.getPicId());
                    intent.putExtra("description",bean.getDoc());
                    intent.setClass(getActivity(), Buy.class);
                    startActivityForResult(intent, 0);
                    getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.scale_fade_out);
                }
            }
        });

        dialog = new LaunchDialog(getActivity());
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.attachToRecyclerView(mRecyclerView);
        fab.setImageResource(R.drawable.ic_tool_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
        mRefreshLayout.setRefreshing(true);
        new GetDataTask().execute(0);
        return view;
    }

    private class GetDataTask extends AsyncTask<Integer, Void, List<Bean>>{
        private int type = 0; //0 represents refresh, 1 represents load

        @Override
        protected List<Bean> doInBackground(Integer... params) {
            if(params.length == 0)
                return null;
            type = params[0];
            if(!NetInfo.checkNetwork(getActivity())){
                return null;
            }
            return getDbData();
        }

        @Override
        protected void onPostExecute(List<Bean> beanList) {
            if(beanList != null) {
                if (type == 0) mAdapter.clear();
                int len = beanList.size();
                for (int i = 0; i < len; ++i) {
                    mAdapter.addItem(beanList.get(i));
                }
            }
            if(type==0){
                mRefreshLayout.setRefreshing(false);
            }else if(type==1){
                mRefreshLayout.setLoading(false);
            }
        }
    }

    private List<Bean> getDbData(){
        MyToken=GlobalParameterApplication.getToken();
        JSONObject data = new JSONObject();
        data.put("startAt",start);
        data.put("length",20);

        Log.i("UsedDeal", "get");
        try {
            URL url = new URL(surl + "/item_get_all");
            Log.i("UsedDeal","getconnection");
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
            int len = result.length();
            start += len;
            List<Bean> dbData = new ArrayList<>();
            for (int i = 0; i < len; i++)
            if (result.getJSONObject(i).getInt("status")==0) {
                String[] rr = result.getJSONObject(i).getString("images").split(",");
                if (rr[0]=="") {
                    BitmapFactory.Options cc = new BitmapFactory.Options();
                    cc.inSampleSize = 20;
                    dbData.add(new Bean(result.getJSONObject(i).getInt("ID"),GlobalParameterApplication.imgSurl+"1B4B907678CCD423", result.getJSONObject(i).getString("caption"), result.getJSONObject(i).getString("description")));
                } else{
                    dbData.add(new Bean(result.getJSONObject(i).getInt("ID"),GlobalParameterApplication.imgSurl+rr[0], result.getJSONObject(i).getString("caption"), result.getJSONObject(i).getString("description"),result.getJSONObject(i).getInt("estimatedPriceByUser")));
                }
            }
            return dbData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

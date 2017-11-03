package com.aixinwu.axw.fragment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aixinwu.axw.Adapter.DealListAdapter;
import com.aixinwu.axw.R;
import com.aixinwu.axw.activity.Buy;
import com.aixinwu.axw.tools.Bean;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.OnRecyclerItemClickListener;
import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.github.clans.fab.FloatingActionMenu;


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

    private MaterialRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private FloatingActionMenu fam;
    private int start = 0;

    public String MyToken;
    public String surl = GlobalParameterApplication.getSurl();

    private static DealListAdapter mAdapter;

    private MaterialRefreshListener mOnClickListener = new MaterialRefreshListener() {
        @Override
        public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
            start = 0;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new GetDataTask().execute(0);
        }

        @Override
        public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new GetDataTask().execute(1);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.used_deal,null);
        mRefreshLayout = (MaterialRefreshLayout) view.findViewById(R.id.homepageScroll2);
        mRefreshLayout.setMaterialRefreshListener(mOnClickListener);
        mRefreshLayout.autoRefresh();

        mAdapter = new DealListAdapter(getActivity());
        mRecyclerView = (RecyclerView)view.findViewById(R.id.deal_item_list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new OnRecyclerItemClickListener(mRecyclerView){
        @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                Intent intent = new Intent();
                Bean bean = ((DealListAdapter.VHolder)vh).getData();
                intent.putExtra("itemId", bean.getItemId());
                intent.putExtra("caption",bean.getType());
                intent.putExtra("pic_url",bean.getPicId());
                intent.putExtra("description",bean.getDoc());
                intent.setClass(getActivity(), Buy.class);
                startActivity(intent);
            }
        });

        fam = (FloatingActionMenu) view.findViewById(R.id.fab_menu);
        fam.setClosedOnTouchOutside(true);
        return view;
    }

    private class GetDataTask extends AsyncTask<Integer, Void, List<Bean>>{
        private int type; //0 represents refresh, 1 represents load

        @Override
        protected List<Bean> doInBackground(Integer... params) {
            if(params.length == 0)
                return null;
            type = params[0];
            return getDbData();
        }

        @Override
        protected void onPostExecute(List<Bean> beanList) {
            if(type==0){
                mAdapter.clear();
            }
            if(beanList == null)return;
            int len = beanList.size();
            for(int i = 0; i < len; ++i){
                mAdapter.addItem(beanList.get(i));
            }
            mAdapter.notifyDataSetChanged();
            if(type==0){
                mRefreshLayout.finishRefresh();
            }else if(type==1){
                mRefreshLayout.finishRefreshLoadMore();
            }
            return;
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

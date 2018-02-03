package com.aixinwu.axw.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.aixinwu.axw.activity.MainActivity;
import com.aixinwu.axw.view.LaunchDialog;
import com.aixinwu.axw.adapter.DealListAdapter;
import com.aixinwu.axw.R;
import com.aixinwu.axw.activity.Buy;
import com.aixinwu.axw.tools.Bean;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.NetInfo;
import com.aixinwu.axw.tools.OnRecyclerItemClickListener;
import com.aixinwu.axw.view.MyRefreshLayout;


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
public class UsedDeal extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    private MyRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private int start = 0;
    private LaunchDialog dialog;
    private FloatingActionButton fab;
    private int offset = 0;
    private View view;

    public String MyToken;
    public String surl = GlobalParameterApplication.getSurl();

    private static DealListAdapter mAdapter;
    private int mode = 0;
    private RotateAnimation rotateLeft, rotateBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_used_deal,null);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.registerOnSharedPreferenceChangeListener(this);
        int mode = Integer.parseInt(preferences.getString(getString(R.string.pref_display_key), getActivity().getString(R.string.pref_display_default)));

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
        mAdapter = new DealListAdapter(getActivity(), mode);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.deal_item_list);
        if(mode == 0) mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        else mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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

        rotateLeft = new RotateAnimation(0.0f,-45.0f,Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotateLeft.setDuration(100);
        rotateLeft.setFillAfter(true);
        rotateBack = new RotateAnimation(-45.0f,0.0f,Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotateBack.setDuration(100);
        rotateBack.setFillAfter(true);

        dialog = new LaunchDialog(getActivity());
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                fab.startAnimation(rotateBack);
            }
        });
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_tool_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.startAnimation(rotateLeft);
                dialog.show();
            }
        });
        offset = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());

        mRefreshLayout.setRefreshing(true);
        new GetDataTask().execute(0);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!getUserVisibleHint())return;
        if(NetInfo.checkNetwork(getActivity()))return;
        Snackbar snackbar = Snackbar.make(view, "网络未连接或不可用,请检查设置", Snackbar.LENGTH_LONG)
                .setAction("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_SETTINGS);
                        getActivity().startActivity(intent);
                    }
                });
        snackbar.addCallback(new Snackbar.Callback(){
            @Override
            public void onShown(Snackbar sb) {
                ViewCompat.setTranslationY(fab, -offset);
                super.onShown(sb);
            }
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                ViewCompat.setTranslationY(fab, 0);
                super.onDismissed(transientBottomBar, event);
            }
        });
        View view = snackbar.getView();
        ((TextView) view.findViewById(R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.white));
        snackbar.show();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(!isAdded())return;
        if (key.equals(getActivity().getString(R.string.pref_display_key))){
            mode = Integer.parseInt(sharedPreferences.getString(key, getActivity().getString(R.string.pref_display_default)));
            if(mode == 0) mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
            else mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mAdapter.changeMode(mode);
        }
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

        try {
            URL url = new URL(surl + "/item_get_all");
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

package com.aixinwu.axw.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aixinwu.axw.activity.MainActivity;
import com.aixinwu.axw.adapter.ProductAdapter;
import com.aixinwu.axw.adapter.VolunteerAdapter;
import com.aixinwu.axw.R;
import com.aixinwu.axw.activity.ProductDetailActivity;
import com.aixinwu.axw.activity.ProductListActivity;
import com.aixinwu.axw.activity.VolActivityList;
import com.aixinwu.axw.activity.VolunteerApply;
import com.aixinwu.axw.model.Product;
import com.aixinwu.axw.model.VolunteerActivity;
import com.aixinwu.axw.tools.ADInfo;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.NetInfo;
import com.aixinwu.axw.tools.ViewFactory;
import com.aixinwu.axw.view.BaseViewPager;
import com.aixinwu.axw.view.CycleViewPager;
import com.aixinwu.axw.view.MyGridView;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangyuding on 2016/4/6.
 */
public class HomePage extends CycleViewPager implements SharedPreferences.OnSharedPreferenceChangeListener{
    private View view;
    private SwipeRefreshLayout refreshLayout;
    private MyGridView gridView1, gridView2, gridView3;
    private RelativeLayout productLayout, leaseLayout, volLayout;
    private ProductAdapter adapter1, adapter2;
    private VolunteerAdapter adapter3;

    private int size = 6;
    private List<ImageView> views = new ArrayList<ImageView>();
    private List<ADInfo> infos = new ArrayList<ADInfo>();
    private String[] imageUrls = {
        	/*"http://aixinwu.sjtu.edu.cn/images/slider/p0.jpg",
        	"http://aixinwu.sjtu.edu.cn/images/slider/p1.jpg",
    		"http://aixinwu.sjtu.edu.cn/images/slider/p2.jpg",*/
            "http://aixinwu.sjtu.edu.cn/images/welcome/main0.jpg",
            "http://aixinwu.sjtu.edu.cn/images/welcome/main1.jpg",
            "http://aixinwu.sjtu.edu.cn/images/welcome/main2.jpg"};

    private List<Product> productList = new ArrayList<Product>();
    private List<Product> leaseList = new ArrayList<Product>();
    private List<VolunteerActivity> volList = new ArrayList<VolunteerActivity>();
    private SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mActivity = getActivity();
        view = inflater.inflate(R.layout.fragment_home_page, null);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.registerOnSharedPreferenceChangeListener(this);
        //继承自父类
        boolean flag = preferences.getBoolean(mActivity.getString(R.string.pref_show_slider_key),
                getResources().getBoolean(R.bool.pref_show_slider_default));
        rollPictureLayout = (RelativeLayout)view.findViewById(R.id.rollPicture);
        viewPager = (BaseViewPager) view.findViewById(R.id.viewPager);
        indicatorLayout = (LinearLayout) view.findViewById(R.id.layout_viewpager_indicator);
        setViewPagerScrollSpeed(1000);//设置滑动速度
        init();
        if(flag){
            rollPictureLayout.setVisibility(View.VISIBLE);
        }else{
            rollPictureLayout.setVisibility(View.GONE);
        }
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        refreshLayout.setColorSchemeResources(typedValue.resourceId);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                views.clear();
                views.add(ViewFactory.getImageView(getActivity(), infos.get(infos.size() - 1).getUrl()));
                for (int i = 0; i < infos.size(); i++) {
                    views.add(ViewFactory.getImageView(getActivity(), infos.get(i).getUrl()));
                }
                views.add(ViewFactory.getImageView(getActivity(), infos.get(0).getUrl()));
                setImageViews(views);
                new GetDataTask().execute(0);
            }
        });
        size = Integer.parseInt(preferences.getString(getString(R.string.pref_size_key), getResources().getString(R.string.pref_size_default)));

        gridView1 = (MyGridView) view.findViewById(R.id.grid1);
        gridView2 = (MyGridView) view.findViewById(R.id.grid2);
        gridView3 = (MyGridView) view.findViewById(R.id.grid3);

        productLayout = (RelativeLayout) view.findViewById(R.id.exchange_more);
        leaseLayout = (RelativeLayout) view.findViewById(R.id.lend_more_more);
        volLayout = (RelativeLayout) view.findViewById(R.id.vol_more_more);
        adapter1 = new ProductAdapter(getActivity(), R.layout.product_item);
        adapter2 = new ProductAdapter(getActivity(), R.layout.product_item);
        adapter3 = new VolunteerAdapter(getActivity(), R.layout.item_volunteer);
        gridView1.setAdapter (adapter1);
        gridView2.setAdapter (adapter2);
        gridView3.setAdapter (adapter3);

        initialize();
        refreshLayout.setRefreshing(true);
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
        View view = snackbar.getView();
        ((TextView) view.findViewById(R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.white));
        snackbar.show();
    }

    private void initialize() {
        for(int i = 0; i < imageUrls.length; i ++){
            ADInfo info = new ADInfo();
            info.setUrl(imageUrls[i]);
            info.setContent("ͼƬ-->" + i );
            infos.add(info);
        }
        views.add(ViewFactory.getImageView(getActivity(), infos.get(infos.size() - 1).getUrl()));
        for (int i = 0; i < infos.size(); i++) {
            views.add(ViewFactory.getImageView(getActivity(), infos.get(i).getUrl()));
        }
        views.add(ViewFactory.getImageView(getActivity(), infos.get(0).getUrl()));
        setCycle(true);
        setData(views, infos, mAdCycleViewListener);
        setWheel(true);
        setIndicatorCenter();

        productLayout.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProductListActivity.class);
                intent.putExtra("type", "exchange");
                getActivity().startActivityForResult(intent, 404);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.scale_fade_out);

            }
        });

        leaseLayout.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProductListActivity.class);
                String s = "rent";
                intent.putExtra("type", s);
                getActivity().startActivityForResult(intent, 404);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.scale_fade_out);
            }
        });

        volLayout.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), VolActivityList.class);
                getActivity().startActivityForResult(intent, 0);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.scale_fade_out);
            }
        });

        gridView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = productList.get(i);
                Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
                intent.putExtra("productId", product.getId());
                getActivity().startActivityForResult(intent, 404);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.scale_fade_out);
            }
        });
        gridView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = leaseList.get(i);
                Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
                intent.putExtra("productId", product.getId());
                getActivity().startActivityForResult(intent, 404);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.scale_fade_out);
            }
        });
        gridView3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                VolunteerActivity product = volList.get(i);
                Intent intent = new Intent(getActivity(), VolunteerApply.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("volActivityId", product);
                intent.putExtras(bundle);
                getActivity().startActivityForResult(intent, 0);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.scale_fade_out);
            }
        });
    }

    private CycleViewPager.ImageCycleViewListener mAdCycleViewListener = new CycleViewPager.ImageCycleViewListener() {

        @Override
        public void onImageClick(ADInfo info, int position, View imageView) {
            if (isCycle()) {
                position = position - 1;
            }
        }

    };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(!isAdded())return;
        if(key.equals(mActivity.getString(R.string.pref_show_slider_key))){
            if(sharedPreferences.getBoolean(key, mActivity.getResources().getBoolean(R.bool.pref_show_slider_default))){
                rollPictureLayout.setVisibility(View.VISIBLE);
            }else{
                rollPictureLayout.setVisibility(View.GONE);
            }
        }
        else if (key.equals(mActivity.getString(R.string.pref_size_key))){
            size = Integer.parseInt(sharedPreferences.getString(key, mActivity.getString(R.string.pref_size_default)));
            new GetDataTask().execute(1);
        }
    }

    private class GetDataTask extends AsyncTask<Integer, Void, Void>{
        Integer flag = 0;

        @Override
        protected Void doInBackground(Integer... params) {
            if(params == null)return null;
            if(!NetInfo.checkNetwork(getActivity())){
                return null;
            }
            flag = params[0];
            productList.clear();
            productList = new ArrayList<> (getDbData("exchange"));
            int psize = productList.size();
            if(psize > size) psize = size;
            productList = productList.subList(0,psize);

            leaseList.clear();
            leaseList =  new ArrayList<> (getDbData("rent"));
            psize = leaseList.size();
            if(psize > size) psize = size;
            leaseList = leaseList.subList(0,psize);

            volList.clear();
            volList =  new ArrayList<> (getVolunteer());
            psize = volList.size();
            if(psize > size) psize = size;
            volList =volList.subList(0,psize);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(productList != null) {
                adapter1.resetList(productList);
                adapter1.notifyDataSetChanged();
            }
            if(leaseList != null) {
                adapter2.resetList(leaseList);
                adapter2.notifyDataSetChanged();
            }
            if(volList != null) {
                adapter3.resetList(volList);
                adapter3.notifyDataSetChanged();
            }
            if(flag == 0) {
                refreshLayout.setRefreshing(false);
            }
        }
    }

    static public List<Product> getDbData(String type){
        List<Product> dbData = new ArrayList<Product>();
        String MyToken= GlobalParameterApplication.getToken();
        String surl = GlobalParameterApplication.getSurl();
        JSONObject itemsrequest = new JSONObject();
        String typestr = "";
        int start = 0;
        itemsrequest.put("startAt", start);
        itemsrequest.put("length", 100);
        switch (type) {
            case "exchange":
                typestr = "置换";
                break;
            case "rent":
                typestr = "租赁";
                break;
            case "cash":
                typestr = "现金";
                break;
        }

        itemsrequest.put("type", typestr);

        try {
            URL url = new URL(surl + "/item_aixinwu_item_get_list");
            Log.i("HomePage","getconnection");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            conn.getOutputStream().write(itemsrequest.toJSONString().getBytes());
            java.lang.String ostr = IOUtils.toString(conn.getInputStream());
            org.json.JSONObject outjson = null;

                JSONArray result = null;
                outjson = new org.json.JSONObject(ostr);
                result = outjson.getJSONArray("items");
                start += result.length();
                for (int i = 0; i < result.length(); i++){
                    String [] imageurl = result.getJSONObject(i).getString("image").split(",");
                    String descdetail = result.getJSONObject(i).getString("desc");
                    String shortdesc = result.getJSONObject(i).getString("short_desc");
                    String despUrl   = result.getJSONObject(i).getString("desp_url");
                    int stock = result.getJSONObject(i).getInt("stock");
                    if ( imageurl[0].equals("") ) {
                        dbData.add(new Product(result.getJSONObject(i).getInt("id"),
                                result.getJSONObject(i).getString("name"),
                                result.getJSONObject(i).getDouble("price"),
                                stock,
                                GlobalParameterApplication.imgSurl+"121000239217360a3d2.jpg",
                                descdetail,
                                shortdesc,
                                despUrl
                        ));
                    } else
                        dbData.add(new Product(result.getJSONObject(i).getInt("id"),
                                result.getJSONObject(i).getString("name"),
                                result.getJSONObject(i).getDouble("price"),
                                stock,
                                GlobalParameterApplication.axwUrl+imageurl[0],
                                descdetail,
                                shortdesc,
                                despUrl
                        ));
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbData;
    }

    static public List<VolunteerActivity> getVolunteer(){
        List<VolunteerActivity> dbData = new ArrayList<VolunteerActivity>();
        String MyToken= GlobalParameterApplication.getToken();
        String surl = GlobalParameterApplication.getSurl();
        JSONObject itemsrequest = new JSONObject();
        itemsrequest.put("token",MyToken);

        try {
            URL url = new URL(surl + "/aixinwu_volunteer_act_get");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            conn.getOutputStream().write(itemsrequest.toJSONString().getBytes());
            java.lang.String ostr = IOUtils.toString(conn.getInputStream());

            JSONArray outjson = new org.json.JSONArray(ostr);
            for (int i1 = 0; i1 < outjson.length(); ++i1){
                int need = outjson.getJSONObject(i1).getInt("num_needed");
                int signed = outjson.getJSONObject(i1).getInt("num_signed");
                dbData.add(new VolunteerActivity(outjson.getJSONObject(i1).getInt("id"),
                        outjson.getJSONObject(i1).getString("image"),
                        outjson.getJSONObject(i1).getString("name"),
                        outjson.getJSONObject(i1).getDouble("pay_cash"),
                        outjson.getJSONObject(i1).getString("work_date"),
                        need,
                        signed,
                        outjson.getJSONObject(i1).getDouble("workload"),
                        outjson.getJSONObject(i1).getString("site"),
                        outjson.getJSONObject(i1).getInt("joined"),
                        outjson.getJSONObject(i1).getString("about"),
                        outjson.getJSONObject(i1).getString("content")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbData;
    }
}

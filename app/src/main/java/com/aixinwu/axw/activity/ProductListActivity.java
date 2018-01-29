package com.aixinwu.axw.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.aixinwu.axw.adapter.ProductListAdapter;
import com.aixinwu.axw.R;
import com.aixinwu.axw.model.Product;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.NetInfo;
import com.aixinwu.axw.tools.OnRecyclerItemClickListener;
import com.aixinwu.axw.view.MyRefreshLayout;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;

public class ProductListActivity extends Activity {
    private MyRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private int times = 0;
    private String type;
    private ProductListAdapter mAdapter;
    private int mode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getStringExtra("type");
        setContentView(R.layout.activity_product_list);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mode = Integer.parseInt(preferences.getString(getString(R.string.pref_display_key), getResources().getString(R.string.pref_display_default)));

        TextView title = (TextView) this.findViewById(R.id.product_list_title);
        switch (type) {
            case "exchange":
                title.setText("置换专区");
                break;
            case "rent":
                title.setText("租赁专区");
                break;
            case "cash":
                title.setText("公益专区");
                break;
        }

        mRefreshLayout = (MyRefreshLayout) findViewById(R.id.refreshLayout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                times = 0;
                new GetDataTask().execute(0);
            }
        });
        mRefreshLayout.setOnLoadListener(new MyRefreshLayout.OnLoadingListener() {
            @Override
            public void onLoad() {
                times++;
                new GetDataTask().execute(1);
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        if(mode == 0) mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        else mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ProductListAdapter(ProductListActivity.this, mode);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new OnRecyclerItemClickListener(mRecyclerView) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                Product p = ((ProductListAdapter.ViewHolder)vh).getData();
                intent.putExtra("productId", p.getId());
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.scale_fade_out);
            }
        });

        mRefreshLayout.setRefreshing(true);
        new GetDataTask().execute(0);
    }

    private class GetDataTask extends AsyncTask<Integer, Void, List<Product>>{
        private int type = 0; //0 represents refresh, 1 represents load

        @Override
        protected List<Product> doInBackground(Integer... params) {
            if(params.length == 0)
                return null;
            type = params[0];
            if(!NetInfo.checkNetwork(ProductListActivity.this)){
                return null;
            }
            return getDbData();
        }

        @Override
        protected void onPostExecute(List<Product> products) {
            if(products !=null){
                if(type==0){
                    mAdapter.clear();
                }
                int len = products.size();
                for(int i = 0; i < len; ++i){
                    mAdapter.addItem(products.get(i));
                }
            }
            if(type==0){
                mRefreshLayout.setRefreshing(false);
            }else if(type==1){
                mRefreshLayout.setLoading(false);
            }
        }
    }

    private List<Product> getDbData() {
        String MyToken = GlobalParameterApplication.getToken();
        String surl = GlobalParameterApplication.getSurl();
        JSONObject itemsrequest = new JSONObject();
        String typestr = "";
        int start = 9 * times;
        itemsrequest.put("startAt", start);
        itemsrequest.put("length", 9);
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
            List<Product> productList = new ArrayList<Product>();
            for (int i = 0; i < result.length(); i++){
                String jsonall = result.getJSONObject(i).toString();

                String[] imageurl = result.getJSONObject(i).getString("image").split(",");
                String descdetail = result.getJSONObject(i).getString("desc");
                String shortdesc = result.getJSONObject(i).getString("short_desc");
                String despUrl = result.getJSONObject(i).getString("desp_url");
                int stock = result.getJSONObject(i).getInt("stock");
                if (imageurl[0].equals("")) {
                    productList.add(new Product(result.getJSONObject(i).getInt("id"),
                            result.getJSONObject(i).getString("name"),
                            result.getJSONObject(i).getDouble("price"),
                            stock,
                            GlobalParameterApplication.imgSurl+"121000239217360a3d2.jpg",
                            descdetail,
                            shortdesc,
                            despUrl
                    ));
                } else
                    productList.add(new Product(result.getJSONObject(i).getInt("id"),
                            result.getJSONObject(i).getString("name"),
                            result.getJSONObject(i).getDouble("price"),
                            stock,
                            GlobalParameterApplication.axwUrl + imageurl[0],
                            descdetail,
                            shortdesc,
                            despUrl
                    ));
            }
            return productList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.scale_fade_in, R.anim.slide_out_bottom);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode == RESULT_OK){
            setResult(RESULT_OK, data);
            finish();
            overridePendingTransition(R.anim.scale_fade_in, R.anim.slide_out_bottom);
        }
    }
}

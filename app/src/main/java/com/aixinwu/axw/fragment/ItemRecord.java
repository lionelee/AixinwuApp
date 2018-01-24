package com.aixinwu.axw.fragment;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aixinwu.axw.adapter.RecordAdapter;
import com.aixinwu.axw.R;
import com.aixinwu.axw.model.Record;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.OnRecyclerItemClickListener;
import com.aixinwu.axw.widget.RecyclerViewDivider;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ItemRecord extends Fragment {

    private RecyclerView recordItem;
    private ArrayList<Record> record = new ArrayList<>();

    private Handler dHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 395932:
                    RecordAdapter recordAdapter = new RecordAdapter(getActivity(),record);
                    recordItem.setAdapter(recordAdapter);
                    recordItem.addOnItemTouchListener(new OnRecyclerItemClickListener(recordItem){
                        @Override
                        public void onItemClick(RecyclerView.ViewHolder vh) {
                            Record record = ((RecordAdapter.ViewHolder)vh).getData();
                            //TODO open an alertdiaog to show info
                        }
                    });
                    break;
            }
        };
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_item_record, null);
        recordItem = (RecyclerView) view.findViewById(R.id.recordItem);
        recordItem.setLayoutManager(new LinearLayoutManager(getActivity()));
        recordItem.addItemDecoration(new RecyclerViewDivider(getActivity()));

        new Thread(new Runnable() {
            @Override
            public void run() {
                getDbData();
                Message msg = new Message();
                msg.what = 395932;
                dHandler.sendMessage(msg);
            }
        }).start();
        return view;
    }


    private void getDbData(){
        String MyToken= GlobalParameterApplication.getToken();
        JSONObject data = new JSONObject();

        data.put("token",MyToken);
        data.put("offset",0);
        data.put("length",0);

        try {
            URL url = new URL(GlobalParameterApplication.getSurl() + "/aixinwu_order_get");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(data.toJSONString().getBytes());
            java.lang.String ostr = IOUtils.toString(conn.getInputStream());
            org.json.JSONObject outjson = new org.json.JSONObject(ostr);
            JSONArray result = outjson.getJSONArray("orders");

            for (int i = 0; i < result.length(); i++){
                org.json.JSONObject a = result.getJSONObject(i);
                org.json.JSONArray abc = a.getJSONArray("items");
                String imgUrl = "";
                for (int j = 0; j < abc.length(); ++j){
                    String abcd = (abc.getJSONObject(j).getString("image")).split(",")[0];
                    if (j == 0)
                        imgUrl = abcd;
                    else
                        imgUrl = imgUrl + "," + abcd;
                }
                record.add(new Record(a.getString("id"),
                        a.getString("customer_id"),
                        a.getString("consignee_id"),
                        a.getString("order_sn"),
                        a.getString("total_product_price"),
                        a.getString("update_at"),
                        imgUrl
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

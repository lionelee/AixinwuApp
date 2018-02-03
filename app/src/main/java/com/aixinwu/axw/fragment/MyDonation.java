package com.aixinwu.axw.fragment;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.model.Record;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.widget.RecyclerViewDivider;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MyDonation extends Fragment {

    private RecyclerView donateListView;
    private ArrayList<HashMap<String,String>> donateMaps = new ArrayList<HashMap<String, String>>();
    private ArrayList<Record> record = new ArrayList<>();


    private Handler dHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 395932:
                    MyDonationAdapter adapter = new MyDonationAdapter(getActivity(),donateMaps);
                    donateListView.setAdapter(adapter);
                    break;
            }
        };
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_donation, null);
        donateListView = (RecyclerView) view.findViewById(R.id.myOwnDonation);
        donateListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        donateListView.addItemDecoration(new RecyclerViewDivider(getActivity()));

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

            try {
                URL url = new URL(GlobalParameterApplication.getSurl() + "/donate_get");
                try {
                    Log.i("UsedDeal", "getconnection");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    conn.getOutputStream().write(data.toJSONString().getBytes());
                    java.lang.String ostr = IOUtils.toString(conn.getInputStream());
                    org.json.JSONObject outjson = null;
                    try {
                        JSONArray result = null;
                        outjson = new org.json.JSONObject(ostr);
                        result = outjson.getJSONArray("records");
                        for (int i = result.length()-1; i >=0 ; i--){
                            org.json.JSONObject tmpJSONObject = result.getJSONObject(i);
                            String desc = tmpJSONObject.getString("desc");
                            String barcode = tmpJSONObject.getString("barcode");
                            String donateTime = tmpJSONObject.getString("produced_at");

                            HashMap<String,String> donateMap = new HashMap<>();
                            donateMap.put("desc",desc);
                            donateMap.put("barcode",barcode);
                            donateMap.put("produced_at",donateTime);
                            donateMaps.add(donateMap);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
    }


    private class MyDonationAdapter extends RecyclerView.Adapter<MyDonationAdapter.ViewHolder> {

        private LayoutInflater mInflater;
        private ArrayList<HashMap<String,String>> donateMaps;


        public MyDonationAdapter(Context context, ArrayList<HashMap<String,String>> donateMaps) {
            mInflater = LayoutInflater.from(context);
            this.donateMaps = donateMaps;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_donation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindData(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return donateMaps.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            TextView desc, barcode, producedAt;

            public ViewHolder(View view){
                super(view);
                desc = (TextView) view.findViewById(R.id.t1);
                barcode = (TextView) view.findViewById(R.id.t2);
                producedAt = (TextView) view.findViewById(R.id.t3);
            }

            public void bindData(int position){
                desc.setText(donateMaps.get(position).get("desc"));
                barcode.setText(donateMaps.get(position).get("barcode"));
                producedAt.setText(donateMaps.get(position).get("produced_at"));
            }
        }
    }
}

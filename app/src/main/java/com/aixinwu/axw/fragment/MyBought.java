package com.aixinwu.axw.fragment;

import android.app.AlertDialog;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aixinwu.axw.adapter.RecordAdapter;
import com.aixinwu.axw.R;
import com.aixinwu.axw.model.Record;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.OnRecyclerItemClickListener;
import com.aixinwu.axw.widget.RecyclerViewDivider;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MyBought extends Fragment {

    private RecyclerView recordItem;
    private ArrayList<Record> record = new ArrayList<>();
    private AlertDialog dialog;
    private HorizontalScrollView scrollView;
    private LinearLayout list;
    private TextView name, price, number;
    private ImageView iv_dismiss;
    private int w, ws, h, psize, padding;

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
                            Record item = ((RecordAdapter.ViewHolder)vh).getData();
                            name.setText(item.getOrder_sn());
                            price.setText(item.getTotal_product_price());
                            number.setText(item.getUpdateTime());
                            final String [] imgUrls = item.getImgUrls().split(",");
                            if(imgUrls.length==1) scrollView.getLayoutParams().width = psize;
                            else scrollView.getLayoutParams().width = ws;
                            generatePicView(list, imgUrls);
                            dialog.show();
                            dialog.getWindow().setLayout(w,h);
                            dialog.getWindow().getDecorView().setPadding(0,padding,0,0);
                        }
                    });

                    break;
            }
        };
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_bought, null);
        recordItem = (RecyclerView) view.findViewById(R.id.recordItem);
        recordItem.setLayoutManager(new LinearLayoutManager(getActivity()));
        recordItem.addItemDecoration(new RecyclerViewDivider(getActivity()));
        View v = getLayoutInflater().inflate(R.layout.dialog_item_record,null);
        dialog = new AlertDialog.Builder(getActivity(), R.style.ItemDialogStyle).setView(v).create();
        scrollView = (HorizontalScrollView) v.findViewById(R.id.hori_scroll);
        list = (LinearLayout) v.findViewById(R.id.img_list);
        name = (TextView) v.findViewById(R.id.tv_item_shopping_cart_name);
        price = (TextView) v.findViewById(R.id.tv_item_shopping_cart_price);
        number = (TextView) v.findViewById(R.id.tv_item_shopping_cart_number);
        iv_dismiss = (ImageView) v.findViewById(R.id.iv_dismiss);
        iv_dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                getDbData();
                Message msg = new Message();
                msg.what = 395932;
                dHandler.sendMessage(msg);
            }
        }).start();

        w = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,280,getResources().getDisplayMetrics());
        ws = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,240,getResources().getDisplayMetrics());
        h = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,600,getResources().getDisplayMetrics());
        psize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,180,getResources().getDisplayMetrics());
        padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,70,getResources().getDisplayMetrics());
        return view;
    }

    private void generatePicView(LinearLayout llGroup, String[] photos) {
        llGroup.removeAllViews();
        for (int i = 0; i < photos.length; ++i) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(psize, psize));
            if(i < photos.length-1)imageView.setPadding(0,0,30,0);
            ImageLoader.getInstance().displayImage(GlobalParameterApplication.axwUrl+photos[i], imageView);
            llGroup.addView(imageView);
        }
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

package com.aixinwu.axw.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aixinwu.axw.adapter.ConfirmOrderAdapter;
import com.aixinwu.axw.R;
import com.aixinwu.axw.database.ProductReadDbHelper;
import com.aixinwu.axw.database.ProductReaderContract;
import com.aixinwu.axw.model.ShoppingCartEntity;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.model.Consignee;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ConfirmOrder extends Activity {

    private ArrayList<ShoppingCartEntity> mDatas = new ArrayList<>();
    public ArrayList<Integer> CheckedProductId = new ArrayList<>();
    private ConfirmOrderAdapter mAdapter;
    private ListView commodityList;
    public ArrayList<JSONObject> OrderedProduct = new ArrayList<>();
    private double mTotalMoney = 0;
    private int size = 0;
    private int orderid = -1;
    private Button order;

    private TextView consigneeName;
    private TextView stuId;
    private TextView phone;

    private List<Consignee> consignees = new ArrayList<>();
    private Consignee commonConsigne;

    private RelativeLayout edit;
    private RelativeLayout submitRelative;

    private TextView submit;
    private TextView cancel;

    private EditText editName;
    private EditText editStuId;
    private EditText editPhone;

    private Handler dHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 532394:
                    Toast.makeText(ConfirmOrder.this,"请检查输入信息是否正确",Toast.LENGTH_SHORT).show();
                    break;
                case 234567:
                    consigneeName.setText(commonConsigne.getName());
                    stuId.setText(commonConsigne.getStuId());
                    phone.setText(commonConsigne.getPhoneNumber());
                    break;
                case 234242 :
                    Toast.makeText(ConfirmOrder.this,"修改成功",Toast.LENGTH_SHORT).show();
                    submitRelative.setVisibility(View.GONE);
                    edit.setVisibility(View.VISIBLE);
                    consigneeName.setVisibility(View.VISIBLE);
                    stuId.setVisibility(View.VISIBLE);
                    phone.setVisibility(View.VISIBLE);
                    editName.setVisibility(View.GONE);
                    editName.setText("");
                    editStuId.setText("");
                    editPhone.setText("");
                    editStuId.setVisibility(View.GONE);
                    editPhone.setVisibility(View.GONE);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            GetAddress();
                            Message msg = new Message();
                            msg.what = 234567;
                            dHandler.sendMessage(msg);
                        }
                    }).start();

                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        editName = (EditText) findViewById(R.id.editName);
        editStuId = (EditText) findViewById(R.id.editStuId);
        editPhone = (EditText) findViewById(R.id.editPhone);

        consigneeName = (TextView) findViewById(R.id.name);
        stuId = (TextView) findViewById(R.id.stuId);
        phone = (TextView) findViewById(R.id.phone);

        submitRelative = (RelativeLayout) findViewById(R.id.submitRelative);

        cancel = (TextView) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitRelative.setVisibility(View.GONE);
                edit.setVisibility(View.VISIBLE);
                consigneeName.setVisibility(View.VISIBLE);
                stuId.setVisibility(View.VISIBLE);
                phone.setVisibility(View.VISIBLE);
                editName.setText("");
                editStuId.setText("");
                editPhone.setText("");
                editName.setVisibility(View.GONE);
                editStuId.setVisibility(View.GONE);
                editPhone.setVisibility(View.GONE);
            }
        });

        submit = (TextView) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                       Message msg = new Message();
                        int status = changeCosingnee();
                        if (status == 0){
                            msg.what=234242;
                            dHandler.sendMessage(msg);
                        }
                        else{
                            msg.what = 532394;
                            dHandler.sendMessage(msg);
                        }
                    }
                }).start();
            }
        });

        edit = (RelativeLayout) findViewById(R.id.editRelative);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitRelative.setVisibility(View.VISIBLE);
                edit.setVisibility(View.GONE);
                consigneeName.setVisibility(View.GONE);
                stuId.setVisibility(View.GONE);
                phone.setVisibility(View.GONE);
                editName.setVisibility(View.VISIBLE);
                editStuId.setVisibility(View.VISIBLE);
                editPhone.setVisibility(View.VISIBLE);

            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                GetAddress();
                Message msg = new Message();
                msg.what = 234567;
                dHandler.sendMessage(msg);
            }
        }).start();

        initData();
        commodityList = (ListView)findViewById(R.id.commodityList);
        mAdapter = new ConfirmOrderAdapter(this,mDatas);
        commodityList.setAdapter(mAdapter);
        ((TextView)findViewById(R.id.totalMoney)).setText("合计：" + mTotalMoney+"爱心币");
        order = (Button)findViewById(R.id.order);
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < CheckedProductId.size(); ++i) {
                    deleteFromDatabase(CheckedProductId.get(i));
                }
                final ProgressDialog progressDialog = new ProgressDialog(ConfirmOrder.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("结算中...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                oThread.start();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.scale_fade_in,R.anim.slide_out_bottom);
        super.onBackPressed();
    }

    private void deleteFromDatabase (int id) {
        ProductReadDbHelper mDbHelper = new ProductReadDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(ProductReaderContract.ProductEntry.TABLE_NAME,
                ProductReaderContract.ProductEntry.COLUMN_NAME_ENTRY_ID+"=?",
                new String[]{id+""}
        );
    }

    public void initData(){
        Intent intent = this.getIntent();
        size = (Integer)intent.getSerializableExtra("size");
        mTotalMoney = (Double)intent.getSerializableExtra("mTotalMoney");
        for (int i = 0; i < size; ++i){
            mDatas.add((ShoppingCartEntity)intent.getSerializableExtra("OrderedData"+i));
            String cid = (String)intent.getSerializableExtra("CheckedProductId"+i);
            Integer a = Integer.parseInt(cid);
            CheckedProductId.add(a);
        }

        createOrderList();
    }


    private void createOrderList () {
        int quant = 0;
        for (int i = 0; i < CheckedProductId.size(); i++) {
            JSONObject orderproduct = new JSONObject();
            String index = String.valueOf(CheckedProductId.get(i));
            ProductReadDbHelper mDbHelper = new ProductReadDbHelper(getApplicationContext());
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            Cursor cursor = db.query(ProductReaderContract.ProductEntry.TABLE_NAME,
                    new String[]{ProductReaderContract.ProductEntry.COLUMN_NAME_NUMBER},
                    ProductReaderContract.ProductEntry.COLUMN_NAME_ENTRY_ID + "=?",
                    new String[] {index},
                    null,
                    null,
                    null
            );
            if (cursor.moveToNext()) {
                quant = cursor.getInt(cursor.getColumnIndex("number"));
            }
            cursor.close();
            orderproduct.put("product_id", CheckedProductId.get(i));
            orderproduct.put("isbook", 0);
            orderproduct.put("quantity", quant);
            OrderedProduct.add(orderproduct);
        }
    }

    private void order(){
        String MyToken= GlobalParameterApplication.getToken();
        String surl = GlobalParameterApplication.getSurl();
        int userid = GlobalParameterApplication.getUserID();
        JSONObject orderrequest = new JSONObject();

        orderrequest.put("token", MyToken);
        orderrequest.put("order_info", OrderedProduct);
        orderrequest.put("consignee_id", userid);

        try {
            URL url = new URL(surl + "/item_aixinwu_item_make_order");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            conn.getOutputStream().write(orderrequest.toJSONString().getBytes());

            java.lang.String ostr = IOUtils.toString(conn.getInputStream());
            org.json.JSONObject outjson = null;
            outjson = new org.json.JSONObject(ostr);
            orderid = outjson.getJSONObject("status").getInt("code");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public Thread oThread = new Thread() {
        @Override
        public void run() {
            super.run();
            order();
            Message msg = new Message();
            msg.what = 1994;
            oHandler.sendMessage(msg);
        }
    };

    public Handler oHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 1994:
                    String dialogContent = "";
                    if (orderid == 0)
                        dialogContent = "商品购买成功";
                    else if(orderid == 5)
                        dialogContent = "爱心币余额不足";
                    else if (orderid == 10)
                        dialogContent = "商品购买数量已达到限购上限，无法购买";
                    else
                        dialogContent = "商品购买失败";
            }
        }
    };

    public void GetAddress(){
        try {
            URL url = new URL(GlobalParameterApplication.getSurl() + "/usr_get_address");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type","application/json");
            JSONObject data = new JSONObject();
            data.put("token",GlobalParameterApplication.getToken());
            conn.getOutputStream().write(data.toJSONString().getBytes());
            String ostr = IOUtils.toString(conn.getInputStream());
            org.json.JSONObject result=null;
            org.json.JSONObject outjson = new org.json.JSONObject(ostr);
            org.json.JSONArray outt=null;
            outt=outjson.getJSONArray("address");

            for (int i = 0; i < outt.length(); ++i){
                consignees.add(new Consignee(
                        outt.getJSONObject(i).getString("consignee"),
                        outt.getJSONObject(i).getString("snum"),
                        outt.getJSONObject(i).getString("mobile"),
                        outt.getJSONObject(i).getInt("id"),
                        outt.getJSONObject(i).getInt("customer_id"),
                        outt.getJSONObject(i).getString("email"),
                        outt.getJSONObject(i).getInt("is_default")
                ));

                if (outt.getJSONObject(i).getInt("is_default")==1){
                    commonConsigne = new Consignee(
                            outt.getJSONObject(i).getString("consignee"),
                            outt.getJSONObject(i).getString("snum"),
                            outt.getJSONObject(i).getString("mobile"),
                            outt.getJSONObject(i).getInt("id"),
                            outt.getJSONObject(i).getInt("customer_id"),
                            outt.getJSONObject(i).getString("email"),
                            outt.getJSONObject(i).getInt("is_default")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int changeCosingnee(){
        int status = -1;
        try {
            URL url = new URL(GlobalParameterApplication.getSurl() + "/usr_set_address");
            String newName = editName.getText().toString();
            String newStuId = stuId.getText().toString();
            String newPhone = editPhone.getText().toString();
            if (newName.length() >= 2 && newStuId.length() >= 10 && newPhone.length()==11){
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json");
                JSONObject data = new JSONObject();
                data.put("token",GlobalParameterApplication.getToken());
                data.put("consignee",newName);
                data.put("snum",newStuId);
                data.put("mobile",newPhone);
                conn.getOutputStream().write(data.toJSONString().getBytes());
                String ostr = IOUtils.toString(conn.getInputStream());
                org.json.JSONObject result=null;
                org.json.JSONObject outjson = new org.json.JSONObject(ostr);
                org.json.JSONArray outt=null;
                status = outjson.getJSONObject("status").getInt("code");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

}

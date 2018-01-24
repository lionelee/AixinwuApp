package com.aixinwu.axw.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aixinwu.axw.R;
import com.aixinwu.axw.tools.GlobalParameterApplication;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CashTransfer extends AppCompatActivity {

    AppCompatButton submit ;
    EditText enter_jaccount;
    EditText amount;

    private Boolean validate = true;
    private String _enter_jaccount;
    private String _amount;

    private int transfer_status = -1;
    private String transfer_desp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_transfer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.transfer_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("爱心币转移");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        enter_jaccount = (EditText)findViewById(R.id.enter_jaccount);
        amount = (EditText)findViewById(R.id.enter_coin);

        submit = (AppCompatButton) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm.isActive())
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                
                 _enter_jaccount = enter_jaccount.getText().toString();
                 _amount = amount.getText().toString();

                validate = true;

                if (_enter_jaccount.isEmpty()){
                    enter_jaccount.setError("请输入对方jaccount账号");
                    validate = false;
                }

                if (_amount.isEmpty()){
                    amount.setError("请输入转账数目");
                    validate = false;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if (validate) {
                            transfer_status = transfer_cash(_enter_jaccount, Double.valueOf(_amount));
                            Message msg = new Message();
                            if (transfer_status == 0)
                                msg.what = 134;
                            else
                                msg.what = 135;
                            nHandler.sendMessage(msg);
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.scale_fade_in,R.anim.slide_out_right);
    }

    public Handler nHandler = new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 134:
                    if (transfer_status == 0){
                        Toast.makeText(CashTransfer.this,"转账成功" ,Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 135:
                    String str = transfer_status + " " + transfer_desp;
                    Toast.makeText(CashTransfer.this, str,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private int transfer_cash(String jaccount, double amount){
        int status = -1;
        String MyToken = GlobalParameterApplication.getToken();
        String surl = GlobalParameterApplication.getSurl();

        JSONObject cashTransfer = new JSONObject();

        cashTransfer.put("token", MyToken);
        cashTransfer.put("jaccount_id",jaccount);
        cashTransfer.put("cash",amount);

        try {
            URL url = new URL(surl + "/AixinwuCashTransfer");
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                Log.i("CashTransfer", cashTransfer.toJSONString());
                conn.getOutputStream().write(cashTransfer.toJSONString().getBytes());

                java.lang.String ostr = IOUtils.toString(conn.getInputStream());
                Log.i("CashTransfer_ostr", ostr);
                org.json.JSONObject outjson = null;
                try{
                    outjson = new org.json.JSONObject(ostr);
                    status = outjson.getJSONObject("status").getInt("code");
                    transfer_desp = outjson.getJSONObject("status").getString("description");
                    System.out.println(outjson);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return status;
    }
}

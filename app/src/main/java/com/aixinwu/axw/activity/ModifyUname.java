package com.aixinwu.axw.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aixinwu.axw.R;
import com.aixinwu.axw.tools.GlobalParameterApplication;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

public class ModifyUname extends AppCompatActivity {

    EditText et_uname;
    Button btn_save;

    String uname = "", nickName = "";

    private Handler dHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 843023:
                    Intent data = new Intent();
                    data.putExtra("newuname", nickName);
                    setResult(RESULT_OK, data);
                    finish();
                    overridePendingTransition(R.anim.scale_fade_in,R.anim.slide_out_right);
                    break;
                case 932466:
                    Toast.makeText(ModifyUname.this,"修改失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_uname);

        Toolbar toolbar = (Toolbar) findViewById(R.id.uname_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("修改昵称");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        uname = getIntent().getStringExtra("uname");

        et_uname = (EditText) findViewById(R.id.input_name);
        et_uname.setHint(uname);
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm.isActive())
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        if(save() == 0) msg.what = 843023;
                        else msg.what = 932466;
                        dHandler.sendMessage(msg);
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

    private int save(){
        int status = -1;
        if(TextUtils.isEmpty(et_uname.getText().toString()))return status;
        nickName = et_uname.getText().toString();
        try {
            String MyToken = GlobalParameterApplication.getToken();
            String surl = GlobalParameterApplication.getSurl();
            JSONObject orderrequest = new JSONObject();
            JSONObject userInfo = new JSONObject();

            orderrequest.put("token", MyToken);
            if (nickName.length() > 0)
                userInfo.put("nickname",nickName);

            orderrequest.put("userinfo",userInfo);
            URL url = new URL(surl + "/usr_update");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            conn.getOutputStream().write(orderrequest.toJSONString().getBytes());

            java.lang.String ostr = IOUtils.toString(conn.getInputStream());
            org.json.JSONObject outjson = new org.json.JSONObject(ostr);
            status = outjson.getJSONObject("status").getInt("code");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

}

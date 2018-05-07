package com.aixinwu.axw.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aixinwu.axw.R;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.Tool;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class ModifyProfile extends AppCompatActivity {

    LinearLayout ll_avatar, ll_name;
    TextView tv_name;
    CircleImageView iv_avatar;
    String avatar="", uname = "";
    String imgUrl;
    ProgressDialog progressDialog;

    private Handler dHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            progressDialog.dismiss();
            switch (msg.what){
                case 843023:
                    avatar = imgUrl;
                    ImageLoader.getInstance().displayImage(GlobalParameterApplication.imgSurl+avatar, iv_avatar);
                    break;
                case 932466:
                    Toast.makeText(ModifyProfile.this,"上传失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("个人资料");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(ModifyProfile.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("上传图片...");

        iv_avatar = (CircleImageView)findViewById(R.id.iv_avatar);
        avatar = getIntent().getStringExtra("headProtrait");
        if(!TextUtils.isEmpty(avatar))
            ImageLoader.getInstance().displayImage(GlobalParameterApplication.imgSurl+avatar, iv_avatar);
        tv_name = (TextView) findViewById(R.id.tv_name);
        uname = getIntent().getStringExtra("uname");
        tv_name.setText(uname);


        ll_avatar = (LinearLayout)findViewById(R.id.profile_avatar);
        ll_name = (LinearLayout)findViewById(R.id.profile_name);
        ll_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModifyProfile.this, PhotoPicker.class);
                startActivityForResult(intent, 1001);
                overridePendingTransition(R.anim.slide_in_right,R.anim.scale_fade_out);
            }
        });

        ll_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModifyProfile.this, ModifyUname.class);
                intent.putExtra("uname",uname);
                startActivityForResult(intent, 1002);
                overridePendingTransition(R.anim.slide_in_right,R.anim.scale_fade_out);
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
        Intent data = new Intent();
        data.putExtra("newuname",uname);
        data.putExtra("newavatar",avatar);
        setResult(RESULT_OK,data);
        finish();
        overridePendingTransition(R.anim.scale_fade_in,R.anim.slide_out_right);
    }

    private String uploadPic(String path){
        Tool tool = new Tool();
        String Picset = "";
        try {
            String imageID = tool.sendFile(GlobalParameterApplication.getSurl(), path);
            Picset = imageID;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Picset;

    }


    private int chgAvatar(){
        int status = -1;
        try {
            String MyToken = GlobalParameterApplication.getToken();
            String surl = GlobalParameterApplication.getSurl();
            JSONObject orderrequest = new JSONObject();
            JSONObject userInfo = new JSONObject();

            orderrequest.put("token", MyToken);
            if (imgUrl.length() > 0)
                userInfo.put("image", imgUrl);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
        case 1001:
            if(resultCode == RESULT_OK){
                progressDialog.show();
                final String path = data.getStringExtra("path");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        imgUrl = uploadPic(path);
                        if (chgAvatar() == 0) msg.what = 843023;
                        else msg.what = 932466;
                            dHandler.sendMessage(msg);
                    }
                }).start();
            }
            break;
        case 1002:
            if(resultCode == RESULT_OK){
                uname = data.getStringExtra("newuname");
                tv_name.setText(uname);
            }
            break;
        default:break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

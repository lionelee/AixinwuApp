package com.aixinwu.axw.activity;

import android.content.Intent;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aixinwu.axw.R;
import com.aixinwu.axw.model.VolunteerActivity;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class VolunteerApply extends AppCompatActivity{

    private VolunteerActivity volunteerActivity;
    private AppBarLayout appBarLayout;
    private String telNumber;
    private TextView love_coin;
    private TextView duration;
    private TextView time;
    private TextView site;
    private TextView numberOfPeople;
    private TextView whetherJoined;
    private EditText getPhoneNumber;
    private Button submit;
    private int need;
    private int signed;
    private TextView volAbout;
    private TextView volContent;
    private RelativeLayout content;

    private ImageView img;
    private String thisTime;

    public android.os.Handler nHandler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 341234:
                    Toast.makeText(VolunteerApply.this,"报名成功",Toast.LENGTH_SHORT).show();
                    onBackPressed();
                    break;
                case 422323:
                    Toast.makeText(VolunteerApply.this,"报名失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_apply);
        volunteerActivity = (VolunteerActivity) getIntent().getSerializableExtra("volActivityId");
        appBarLayout = (AppBarLayout) findViewById(R.id.volunteer_appbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.volunteer_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout layout = (CollapsingToolbarLayout)findViewById(R.id.volunteer_layout);
        layout.setTitleEnabled(true);
        layout.setTitle(volunteerActivity.getName());

        img = (ImageView)findViewById(R.id.image);
        love_coin = (TextView) findViewById(R.id.love_coin);
        duration = (TextView) findViewById(R.id.duration);
        time = (TextView) findViewById(R.id.time);
        site =  (TextView) findViewById(R.id.site);
        numberOfPeople = (TextView) findViewById(R.id.numOfPeople);
        submit = (Button) findViewById(R.id.submit);
        getPhoneNumber = (EditText) findViewById(R.id.phoneNumber);
        whetherJoined = (TextView) findViewById(R.id.whetherJoined);
        volAbout = (TextView) findViewById(R.id.volAbout);
        volContent = (TextView) findViewById(R.id.volContent);
//        content = (RelativeLayout) findViewById(R.id.content);
        getPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appBarLayout.setExpanded(false,true);
            }
        });

        if (volunteerActivity.getAbout().length() != 0) {
            volAbout.setText(volunteerActivity.getAbout());
            volContent.setText(Html.fromHtml(volunteerActivity.getContent()));
        }
        else{
//            content.setVisibility(View.GONE);
        }

        need = volunteerActivity.getNeededPeople();
        signed = volunteerActivity.getSignedPeople();

        if (!volunteerActivity.getImg_url().equals(""))
            ImageLoader.getInstance().displayImage(volunteerActivity.getImg_url(),img);

        if (volunteerActivity.getJoined() == 1){
            whetherJoined.setText("已参加");
            whetherJoined.setTextColor(getResources().getColor(R.color.accent));
            submit.setEnabled(false);
        }


        thisTime = volunteerActivity.getTime();

        love_coin.setText("爱心币： +"+volunteerActivity.getPayback());
        duration.setText("时长："+volunteerActivity.getDuration()+"小时");
        time.setText("时间："+thisTime.substring(5,10)+" "+thisTime.substring(11,16));
        site.setText("地址："+volunteerActivity.getSite());

        if (need <= signed && need != 0){
//            submit.setEnabled(false);
            numberOfPeople.setText("人数已满："+signed+"/"+need);
            numberOfPeople.setTextColor(getResources().getColor(R.color.accent));
        }
        else {
            if (need == 0)
                numberOfPeople.setText("人数：" + signed + "/∞");
            else
                numberOfPeople.setText("人数：" + signed + "/" + need);
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                telNumber = getPhoneNumber.getText().toString();
                if (GlobalParameterApplication.getLogin_status() == 0)
                    Toast.makeText(VolunteerApply.this,"请先登录",Toast.LENGTH_SHORT).show();
                else if (telNumber.length() == 11){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int status = signUpActivity();
                            Message msg = new Message();
                            if (status == 0)
                                msg.what = 341234;
                            else
                                msg.what = 422323;

                            nHandler.sendMessage(msg);

                        }
                    }).start();

                }
                else
                    Toast.makeText(VolunteerApply.this,"手机号输入错误",Toast.LENGTH_SHORT).show();

            }
        });


    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.scale_fade_in, R.anim.slide_out_bottom);
        super.onBackPressed();
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

    private int signUpActivity(){
        int status = 0;
        String MyToken= GlobalParameterApplication.getToken();
        String surl = GlobalParameterApplication.getSurl();
        JSONObject volSignUp = new JSONObject();

        volSignUp.put("token",GlobalParameterApplication.getToken());
        volSignUp.put("project_id",volunteerActivity.getId());
        volSignUp.put("work_date",thisTime);
        volSignUp.put("tel",telNumber);

        try {
            URL url = new URL(surl + "/aixinwu_volunteer_act_Join");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(volSignUp.toJSONString().getBytes());
            java.lang.String ostr = IOUtils.toString(conn.getInputStream());
            org.json.JSONObject outjson = new org.json.JSONObject(ostr);
            status = outjson.getJSONObject("status").getInt("code");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }
}

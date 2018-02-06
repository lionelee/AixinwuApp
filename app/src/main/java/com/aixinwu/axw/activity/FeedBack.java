package com.aixinwu.axw.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aixinwu.axw.R;
import com.aixinwu.axw.tools.GlobalParameterApplication;

public class FeedBack extends AppCompatActivity {

    private EditText et_phone;
    private EditText et_suggest;
    private Button btn_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);
        Toolbar toolbar = (Toolbar) findViewById(R.id.feedback_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("意见反馈");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        et_phone = (EditText) findViewById(R.id.phone);
        et_suggest = (EditText) findViewById(R.id.suggestion);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GlobalParameterApplication.getLogin_status()==0){
                    Intent intent = new Intent(FeedBack.this, LoginActivity.class);
                    startActivityForResult(intent,0);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.scale_fade_out);
                }else{
                    String phone = et_phone.getText().toString();
                    String suggestion = et_suggest.getText().toString();
                    if(TextUtils.isEmpty(suggestion)){
                        Toast.makeText(FeedBack.this,"请输入内容",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String str = "联系方式:"+phone+"\n意见:"+suggestion;
                    GlobalParameterApplication.publish(str,1525);
                    onBackPressed();
                }
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
}

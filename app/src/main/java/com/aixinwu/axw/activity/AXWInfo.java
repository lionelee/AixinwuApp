package com.aixinwu.axw.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.aixinwu.axw.R;

public class AXWInfo extends AppCompatActivity {

    ImageView qrApp;
    ImageView qrWechat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_axwinfo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.axwinfo_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("关于爱心屋");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        qrApp = (ImageView) findViewById(R.id.qrAPP);
        qrWechat = (ImageView) findViewById(R.id.qrWechat);

        qrApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(), RawPictureActivity.class);
                intent.putExtra("img","APP");
                if(Build.VERSION.SDK_INT >= 21) {
                    qrApp.setTransitionName("imgView");
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            AXWInfo.this, qrApp, "imgView");
                    startActivity(intent,optionsCompat.toBundle());
                    overridePendingTransition(0,0);
                }else{
                    startActivityForResult(intent,0);
                    overridePendingTransition(R.anim.alpha_fade_in,R.anim.alpha_fade_out);
                }
            }
        });

        qrWechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(), RawPictureActivity.class);
                intent.putExtra("img","wechat");
                if(Build.VERSION.SDK_INT >= 21) {
                    qrWechat.setTransitionName("imgView");
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            AXWInfo.this, qrWechat, "imgView");
                    startActivity(intent,optionsCompat.toBundle());
                    overridePendingTransition(0,0);
                }else{
                    startActivityForResult(intent,0);
                    overridePendingTransition(R.anim.alpha_fade_in,R.anim.alpha_fade_out);
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

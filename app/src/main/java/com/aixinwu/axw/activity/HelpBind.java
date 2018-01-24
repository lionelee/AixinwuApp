package com.aixinwu.axw.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.aixinwu.axw.R;

/**
 * Created by lionel on 2017/11/10.
 */

public class HelpBind extends AppCompatActivity {

    private WebView webView;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_bind);

        Toolbar toolbar = (Toolbar) findViewById(R.id.help_bind_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("注册与JAccount绑定");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pb = (ProgressBar) findViewById(R.id.help_bind_pb);
        webView = (WebView) findViewById(R.id.help_bind_website);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress==100){
                    pb.setVisibility(View.GONE);//加载完网页进度条消失
                }
                else{
                    pb.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    pb.setProgress(newProgress);//设置进度值
                }

            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://mp.weixin.qq.com/s/jLzyrkCv9ZbowaX_GFDnoA");
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

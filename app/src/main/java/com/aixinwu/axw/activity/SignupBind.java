package com.aixinwu.axw.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.aixinwu.axw.R;
import com.aixinwu.axw.view.MyWebView;

/**
 * Created by lionel on 2017/11/10.
 */

public class SignupBind extends AppCompatActivity {

    private SwipeRefreshLayout layout;
    private MyWebView webView;
    private ProgressBar pb;
    private String Url = "https://tusenpo.github.io/FlappyFrog/?from=singlemessage&isappinstalled=0";
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_bind);

        if(getIntent().getStringExtra("url") != null){
            Url = getIntent().getStringExtra("url");
            flag = false;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.help_bind_toolbar);
        setSupportActionBar(toolbar);
        if (flag) getSupportActionBar().setTitle("Excited!!!");
        else getSupportActionBar().setTitle("注册与JAccount绑定");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layout = (SwipeRefreshLayout) findViewById(R.id.help_bind_layout);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue,true);
        layout.setColorSchemeResources(typedValue.resourceId);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                layout.setRefreshing(false);
                webView.loadUrl(Url);
            }
        });
        pb = (ProgressBar) findViewById(R.id.help_bind_pb);
        webView = (MyWebView) findViewById(R.id.help_bind_website);
        webView.setLayout(layout);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress==100){
                    pb.setVisibility(View.GONE);
                } else{
                    pb.setVisibility(View.VISIBLE);
                    pb.setProgress(newProgress);
                }

            }
        });
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                view.loadUrl("file:///android_asset/error.html");
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(Url);
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

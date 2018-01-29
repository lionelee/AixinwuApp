package com.aixinwu.axw.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.view.MyWebView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Sign extends AppCompatActivity {

    private SwipeRefreshLayout layout;
    private MyWebView webView;
    private TextView tv_msg;
    private ProgressBar pb;
    private boolean flag =false;

    ValueCallback<String> callback = new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String s) {
            final String str = s.replace("&nbsp;","").replace(" ","")
                    .replace("\"","").replace("\\n","")
                    .replace("--","\n\n").replace("ღ","\nღ")
                    .replace("登陆","签到");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText(str);
                }
            });
        }
    };

    String js = "(function(){" +
            "var str = document.getElementsByClassName(\"header_userInfo_word\")[1].textContent;" +
            "var ul = document.getElementsByClassName(\"header_userInfo_box\")[0];"+
            "str += \"--\";"+
            "str += ul.getElementsByTagName('li')[0].textContent;" +
            "return str;})();";

    private String Url = "http://aixinwu.sjtu.edu.cn/index.php/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        Toolbar toolbar = (Toolbar) findViewById(R.id.sign_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("签到");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        pb = (ProgressBar) findViewById(R.id.sign_pb);
        tv_msg = (TextView) findViewById(R.id.tv_msg);
        layout = (SwipeRefreshLayout) findViewById(R.id.sign_refreshlayout);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        layout.setColorSchemeResources(typedValue.resourceId);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                layout.setRefreshing(false);
                webView.loadUrl(Url);
            }
        });
        webView = (MyWebView) findViewById(R.id.sign_website);
        webView.setLayout(layout);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress==100){
                    pb.setVisibility(View.GONE);
                }else{
                    pb.setVisibility(View.VISIBLE);
                    pb.setProgress(newProgress);
                }

            }
        });
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if(url.equals("http://aixinwu.sjtu.edu.cn/index.php/home")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.setVisibility(View.GONE);
                            tv_msg.setVisibility(View.VISIBLE);
                            layout.setEnabled(false);
                        }
                    });
                    if(!preferences.getBoolean("sign",false)){
                        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
                        int d = Integer.parseInt(date);
                        preferences.edit().putInt("date",d).putBoolean("sign",true).commit();
                        flag = true;
                    }
                }else{
                    super.onPageStarted(view, url, favicon);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(url.equals("http://aixinwu.sjtu.edu.cn/index.php/home")){
                    webView.evaluateJavascript(js,callback);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                super.shouldOverrideUrlLoading(view, request);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl("file:///android_asset/error.html");
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
//        if(webView.canGoBack()){
//            webView.goBack();
//        }else{
            Intent data = new Intent();
            data.putExtra("sign",flag);
            setResult(RESULT_OK,data);
            finish();
            overridePendingTransition(R.anim.scale_fade_in,R.anim.slide_out_right);
//        }
    }
}

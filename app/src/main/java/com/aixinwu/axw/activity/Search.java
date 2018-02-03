package com.aixinwu.axw.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.view.MyWebView;


/**
 * Created by lionel on 2017/11/10.
 */

public class Search extends AppCompatActivity {

    private EditText et_search;
    private ImageView iv_cancel;
    private SwipeRefreshLayout layout;
    private MyWebView webView;
    private ProgressBar pb;
    private String Url = "http://aixinwu.sjtu.edu.cn/index.php/search";
    private String FormData = "";
    String js = "(function(){" +
            "document.getElementsByTagName('div')[2].style.display='none';" +
            "var p = document.body.children;for(var i = 0; i < p.length; ++i){" +
            "if(p[i].className=='Main'){var s = p[i].children;" +
            "for(var j = 0; j < s.length; ++j){if(s[j].className!='right')" +
            "s[j].style.display='none'}p[i].className='';}else p[i].style.display='none';}})();";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        et_search = (EditText)findViewById(R.id.et_search);
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str= et_search.getText().toString();
                if(TextUtils.isEmpty(str)){
                    iv_cancel.setVisibility(View.INVISIBLE);
                } else {
                    iv_cancel.setVisibility(View.VISIBLE);
                }
            }
        });
//        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//                String str= et_search.getText().toString();
//                if(TextUtils.isEmpty(str))return false;
//                if(i == EditorInfo.IME_ACTION_SEARCH){
//                    FormData = "sousuo="+str;
//                    webView.postUrl(Url,FormData.getBytes());
//                }
//                return false;
//            }
//        });
        iv_cancel = (ImageView)findViewById(R.id.iv_cancel);
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_search.setText("");
            }
        });


//        layout = (SwipeRefreshLayout) findViewById(R.id.search_layout);
//        layout.setEnabled(false);
//        TypedValue typedValue = new TypedValue();
//        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue,true);
//        layout.setColorSchemeResources(typedValue.resourceId);
//        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                layout.setRefreshing(false);
//                webView.postUrl(Url,FormData.getBytes());
//            }
//        });
//        pb = (ProgressBar) findViewById(R.id.search_pb);
//        webView = (MyWebView) findViewById(R.id.search_website);
//        webView.setLayout(layout);
//        webView.setWebChromeClient(new WebChromeClient(){
//            @Override
//            public void onProgressChanged(WebView view, int newProgress) {
//                if(newProgress==100){
//                    pb.setVisibility(View.GONE);
//                } else{
//                    pb.setVisibility(View.VISIBLE);
//                    pb.setProgress(newProgress);
//                }
//
//            }
//        });
//        webView.setWebViewClient(new WebViewClient(){
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                webView.evaluateJavascript(js,null);
//            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    view.loadUrl(request.getUrl().toString());
//                } else {
//                    view.loadUrl(request.toString());
//                }
//                return true;
//            }
//
//            @Override
//            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//                super.onReceivedError(view, request, error);
//                view.loadUrl("file:///android_asset/error.html");
//            }
//        });
//
//        WebSettings webSettings = webView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
//        webSettings.setUseWideViewPort(true);
//        webSettings.setLoadWithOverviewMode(true);
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
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm.isActive())
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        finish();
        overridePendingTransition(R.anim.alpha_fade_in,R.anim.alpha_fade_out);
    }
}

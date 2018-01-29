package com.aixinwu.axw.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aixinwu.axw.R;
import com.aixinwu.axw.adapter.CommentAdapter;
import com.aixinwu.axw.tools.GlobalParameterApplication;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by lionel on 2018/1/24.
 */

public class Comment extends AppCompatActivity {

    private String surl = GlobalParameterApplication.getSurl();
    private String MyToken = GlobalParameterApplication.getToken();
    private SwipeRefreshLayout refreshLayout;
    private ListView lv_comments;
    private EditText et_comment;
    private ImageView iv_send;

    private int itemID;
    private int csize;
    private String commentwords;
    private ArrayList<String> comment_texts;
    private ArrayList<String> comment_times;
    private ArrayList<HashMap<String,String>> comment_list = new ArrayList<>();
    private CommentAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent data = getIntent();
        itemID = data.getIntExtra("itemID",0);
        if(itemID==0)onBackPressed();
        if(data.getStringArrayListExtra("comment_texts") == null){
            comment_texts = new ArrayList<>();
        }else{
            comment_texts = data.getStringArrayListExtra("comment_texts");
        }
        if(data.getStringArrayListExtra("comment_times") == null){
            comment_times = new ArrayList<>();
        }else{
            comment_times = data.getStringArrayListExtra("comment_times");
        }
        for(int i = 0; i < comment_texts.size(); ++i){
            HashMap<String, String> map = new HashMap<>();
            map.put("comment",comment_texts.get(i));
            map.put("time",comment_times.get(i));
            comment_list.add(map);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.comment_toolbar);
        setSupportActionBar(toolbar);
        csize = comment_list.size();
        getSupportActionBar().setTitle("留言("+csize+")");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.comment_layout);
        lv_comments = (ListView) findViewById(R.id.comments);
        et_comment = (EditText) findViewById(R.id.et_comment);
        et_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(TextUtils.isEmpty(et_comment.getText().toString())){
                    iv_send.setClickable(false);
                    iv_send.setImageResource(R.drawable.comm_send_disable);
                } else{
                    iv_send.setClickable(true);
                    iv_send.setImageResource(R.drawable.comm_send_enable);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        iv_send = (ImageView) findViewById(R.id.iv_send);
        iv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(et_comment.getText().toString()))return;
                commentwords = et_comment.getText().toString();
                new AddCommTask().execute();
            }
        });
        adapter = new CommentAdapter(Comment.this, comment_list);
        lv_comments.setAdapter(adapter);
    }

    private class AddCommTask extends AsyncTask<Void,Void,Integer>{

        @Override
        protected Integer doInBackground(Void... voids) {
            addComment();
            return 1;
        }

        @Override
        protected void onPostExecute(Integer i) {
            if(i<0){
                Toast.makeText(Comment.this,"发送失败",Toast.LENGTH_SHORT).show();
                return;
            }else {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                comment_texts.add(commentwords);
                comment_times.add(time);
                adapter.addItem(commentwords, time);
                adapter.notifyDataSetChanged();
                ++csize;
                getSupportActionBar().setTitle("留言(" + csize + ")");
            }
            et_comment.setText("");
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm.isActive())
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
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
        data.putStringArrayListExtra("comment_texts",comment_texts);
        data.putStringArrayListExtra("comment_times",comment_times);
        setResult(RESULT_OK, data);
        finish();
        overridePendingTransition(R.anim.scale_fade_in,R.anim.slide_out_right);
    }

    private void addComment(){
        try {
            URL url = new URL(surl + "/item_add_comment");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            JSONObject data = new JSONObject();
            data.put("token", MyToken);
            JSONObject comment = new JSONObject();
            comment.put("itemID", itemID);
            comment.put("content", commentwords);
            data.put("comment", comment);
            conn.getOutputStream().write(data.toJSONString().getBytes());
            String ostr = IOUtils.toString(conn.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

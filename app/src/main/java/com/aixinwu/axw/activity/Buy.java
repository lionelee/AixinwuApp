package com.aixinwu.axw.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.aixinwu.axw.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import com.aixinwu.axw.database.Sqlite;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.MyAlertDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by liangyuding on 2016/4/15.
 */
public class Buy extends AppCompatActivity{
    private final String surl = GlobalParameterApplication.getSurl();
    public  java.lang.String MyToken;
    private int itemID;
    private int OwnerID;
    private String Desc;
    private int Price;
    private String Picset;

    private String ownerName;

    private String picId;
    private String description;

    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private EditText commentword;
    private LinearLayout commentsubmit;
    private ListView comments;
    private TextView button2;
    private String[] picts;
    private List<String> pic_list;
    private SimpleAdapter com_adapter;
    private Context mContext;
    private String commentwords;
    private ArrayList<String> Comments = new ArrayList<String>();
    private ArrayList<String> comment_times = new ArrayList<String>();
    private ArrayList<HashMap<String,Object>> comment_list;
    private TextView Caption;
    private String _caption;
    private LinearLayout pictures;

    private String imgUrl;

    private LinearLayout relativeLayoutCollect;
    private ImageView iv_collect;
    private TextView tv_collect;
    private boolean flag = false;

    private Sqlite userDbHelper = new Sqlite(this);

    private CircleImageView headProtrait;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);
        Toolbar toolbar = (Toolbar) findViewById(R.id.buy_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final Intent intent=getIntent();
        Bundle out = intent.getExtras();
        itemID=(int)out.get("itemId");
        _caption = out.getString("caption");
        picId = out.getString("pic_url");
        description = out.getString("description");
        pictures = (LinearLayout) findViewById(R.id.pics);
        button2=(TextView)findViewById(R.id.chat);
        textView1 = (TextView)findViewById(R.id.ownerid);
        textView2 = (TextView)findViewById(R.id.desc);
        textView3 = (TextView)findViewById(R.id.price3);
        textView4 = (TextView)findViewById(R.id.shuoming);
        commentword = (EditText)findViewById(R.id.commentword);
        commentsubmit = (LinearLayout) findViewById(R.id.commentsubmit);
        comments = (ListView)findViewById(R.id.comments);

        headProtrait = (CircleImageView) findViewById(R.id.img_activity_product);
        Caption = (TextView)findViewById(R.id.caption);
        Caption.setText(_caption);
        pic_list = new ArrayList<String>();
        comment_list=new ArrayList<HashMap<String, Object>>();
        mContext = this;


        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = userDbHelper.getWritableDatabase();
                Cursor cursor = db.rawQuery("select * from AXWcollect where itemId = " + itemID + " and userName = '" + GlobalParameterApplication.getUser_name() + "'", null);
                while (cursor.moveToNext()) {
                    Message msg=new Message();
                    msg.what=521521;
                    dHandler.sendMessage(msg);
                    break;
                }
                cursor.close();
                db.close();
            }
        }).start();

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GlobalParameterApplication.getLogin_status()==0){
                    Intent intent3 = new Intent(Buy.this, LoginActivity.class);
                    startActivityForResult(intent3,0);
                    overridePendingTransition(R.anim.slide_in_bottom, R.anim.scale_fade_out);
                } else {
                    Intent intent2 = new Intent(Buy.this,Chattoother.class);
                    intent2.putExtra("itemID",itemID);
                    intent2.putExtra("To",OwnerID);
                    intent2.putExtra("ToName",ownerName);
                    startActivityForResult(intent2,0);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.scale_fade_out);
                }

            }
        });
        commentsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                        final EditText inputServer = new EditText(Buy.this);
                        inputServer.setFocusable(true);

                        final MyAlertDialog confirmDialog = new MyAlertDialog(Buy.this, "", "提交评论", "×",nhandler);
                        confirmDialog.setView(inputServer);
                        confirmDialog.show();
                    }

        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyToken=GlobalParameterApplication.getToken();
                GetComments(itemID);
                GetInfo(itemID);
                HashMap<String,String> usrInfo = ChatList.getUserName("" + OwnerID);
                ownerName = usrInfo.get("usrName");
                imgUrl = usrInfo.get("img");
                picts = Picset.split(",");
                pic_list.clear();
                for (int i = 0; i < picts.length; i++){


                    pic_list.add(GlobalParameterApplication.imgSurl+picts[i]);
                }
                comment_list.clear();
                for (int i = 0; i < Comments.size();i++){
                    HashMap<String,Object> map = new HashMap<String, Object>();
                    map.put("comment",Comments.get(i));
                    map.put("time",comment_times.get(i));
                    comment_list.add(map);
                }
                Message msg=new Message();
                msg.what=2310231;
                nhandler.sendMessage(msg);

            }
        }).start();
        iv_collect = (ImageView) findViewById(R.id.iv_collect);
        tv_collect = (TextView) findViewById(R.id.tv_collect);
        relativeLayoutCollect = (LinearLayout) findViewById(R.id.relativeCollect);
        relativeLayoutCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(flag){
                    try {
                        SQLiteDatabase db = userDbHelper.getWritableDatabase();
                        db.execSQL("delete from AXWcollect where itemId = " + itemID +" and userName='"+GlobalParameterApplication.getUser_name()+"'");
                        db.close();
                        flag = false;
                        iv_collect.setImageResource(R.drawable.ic_star);
                        tv_collect.setTextColor(getResources().getColor(R.color.gray));
                        tv_collect.setText("收藏");
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }else{
                    try {
                        SQLiteDatabase db = userDbHelper.getWritableDatabase();
                        db.execSQL("insert into AXWcollect(itemId,userName,type,desc,picUrl,price) values(" +itemID+",'"+GlobalParameterApplication.getUser_name() + "','" + _caption + "','" + description + "','" + picId + "',"+Price+")");
                        db.close();
                        flag = true;
                        iv_collect.setImageResource(R.drawable.ic_stared);
                        tv_collect.setTextColor(getResources().getColor(R.color.accent));
                        tv_collect.setText("已收藏");
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public Handler dHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 521521:
                    flag = true;
                    iv_collect.setImageResource(R.drawable.ic_stared);
                    tv_collect.setTextColor(getResources().getColor(R.color.accent));
                    tv_collect.setText("已收藏");
                    break;
        }
    };

};

    Handler nhandler = new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(textView1!=null){
                textView4.setText("留言("+Comments.size()+")");


                commentwords = (String) msg.obj;

                switch (msg.what) {

                    case 2323232:
                        new Thread(new Runnable() {
                            @Override
                            public void run(){
                                URL url = null;
                                try {
                                    url = new URL(surl + "/item_add_comment");
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    try {

                                        conn.setRequestMethod("POST");
                                    } catch (ProtocolException e) {
                                        e.printStackTrace();
                                    }
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
                                    System.out.println(ostr);
                                    GetComments(itemID);
                                    comment_list.clear();
                                    for (int i = 0; i < Comments.size();i++){
                                        HashMap<String,Object> map = new HashMap<String, Object>();
                                        map.put("comment",Comments.get(i));
                                        map.put("time",comment_times.get(i));
                                        comment_list.add(map);
                                    }
                                    Message Msg = new Message();
                                    Msg.what = 231123;
                                    nhandler.sendMessage(Msg);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                            }
                        }).start();
                        break;
                    case 2310231:

                        flag=false;
                        textView1.setText(ownerName);
                        textView2.setText(Desc);
                        textView3.setText("价格：￥"+Price);

                        if (!imgUrl.equals(""))
                            ImageLoader.getInstance().displayImage(GlobalParameterApplication.imgSurl+imgUrl, headProtrait);

                        com_adapter=new SimpleAdapter(mContext,comment_list,R.layout.commentitem,new String[]{"comment","time"},new int[]{R.id.comment_text,R.id.comment_time});
                        com_adapter.setViewBinder(new SimpleAdapter.ViewBinder(){
                            @Override
                            public boolean setViewValue(View view, Object o, String s) {
                                if (view instanceof TextView && o instanceof String){
                                    TextView i = (TextView) view;
                                    i.setText((String) o);
                                    return true;
                                }
                                return false;
                            }
                        });
                        comments.setAdapter(com_adapter);
                        comments.setVisibility(View.VISIBLE);
                        int totalHeight = 0;
                        for (int i = 0; i < com_adapter.getCount(); i++) {
                            View listItem = com_adapter.getView(i, null, comments);
                            listItem.measure(0, 0);
                            totalHeight += listItem.getMeasuredHeight();
                        }

                        ViewGroup.LayoutParams params = comments.getLayoutParams();
                        params.height = totalHeight + (comments.getDividerHeight() * (com_adapter.getCount() - 1));
                        // params.height = params.height;
                        comments.setLayoutParams(params);

                        for (int i = 0; i < pic_list.size(); ++i){
                            ImageView img = new ImageView(mContext);
                            img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            LinearLayout.LayoutParams imgLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                            ImageLoader.getInstance().displayImage(pic_list.get(i),img);
                            img.setLayoutParams(imgLayoutParams);
                            pictures.addView(img,imgLayoutParams);
                        }
                        break;
                    case 231123:
                        //comments.setAdapter(com_adapter);
                        commentword.setText("");
                        int totalHeight1 = 0;
                        for (int i = 0; i < com_adapter.getCount(); i++) {
                            View listItem = com_adapter.getView(i, null, comments);
                            listItem.measure(0, 0);
                            totalHeight1 += listItem.getMeasuredHeight();
                        }

                        ViewGroup.LayoutParams params11 = comments.getLayoutParams();
                        params11.height = totalHeight1 + (comments.getDividerHeight() * (com_adapter.getCount() - 1));
                        comments.setLayoutParams(params11);
                        com_adapter.notifyDataSetChanged();
                        break;
                }
            }

        }

    };


    public void GetComments(int itemID){
        try {
            URL url = new URL(surl + "/item_get_comment");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type","application/json");
            JSONObject data = new JSONObject();
            data.put("token",MyToken);
            JSONObject comment = new JSONObject();
            comment.put("itemID",itemID);
            data.put("comment",comment);
            conn.getOutputStream().write(data.toJSONString().getBytes());
            String ostr = IOUtils.toString(conn.getInputStream());
            org.json.JSONObject outjson = new org.json.JSONObject(ostr);
            JSONArray result=outjson.getJSONArray("comment");
            org.json.JSONObject outt=null;
            Comments.clear();
            comment_times.clear();
            for (int i = 0; i < result.length();i++){
                    outt=result.getJSONObject(i);
                    Comments.add(outt.getString("content"));
                    comment_times.add(outt.getString("created"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void GetInfo(int itemID) {
        try {
            URL url = new URL(surl + "/item_get");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type","application/json");
            JSONObject data = new JSONObject();
            JSONObject iteminfo = new JSONObject();
            iteminfo.put("ID",itemID);
            data.put("itemInfo",iteminfo);
            conn.getOutputStream().write(data.toJSONString().getBytes());
            String ostr = IOUtils.toString(conn.getInputStream());
            org.json.JSONObject outjson = new org.json.JSONObject(ostr);

            OwnerID = outjson.getJSONObject("itemInfo").getInt("ownerID");

            Desc = outjson.getJSONObject("itemInfo").getString("description");
            Price = outjson.getJSONObject("itemInfo").getInt("estimatedPriceByUser");
            Picset = outjson.getJSONObject("itemInfo").getString("images");
        } catch (Exception e) {
            e.printStackTrace();
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
        finish();
        overridePendingTransition(R.anim.scale_fade_in, R.anim.slide_out_bottom);
        super.onBackPressed();
    }
}

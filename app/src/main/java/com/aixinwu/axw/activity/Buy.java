package com.aixinwu.axw.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aixinwu.axw.R;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import com.aixinwu.axw.database.Sqlite;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.NetInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by liangyuding on 2016/4/15.
 */
public class Buy extends AppCompatActivity{
    private String surl = GlobalParameterApplication.getSurl();
    private String MyToken = GlobalParameterApplication.getToken();
    private int itemID;
    private int OwnerID;
    private String Desc;
    private int Price;
    private String Picset;
    private String ownerName;
    private String picId;
    private String description;
    private String imgUrl;
    private ArrayList<String> comment_texts = new ArrayList<>();
    private ArrayList<String> comment_times = new ArrayList<>();

    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private LinearLayout commentsubmit;
    private TextView tv_comm;
    private TextView button2;
    private String[] picts;
    private List<String> pic_list = new ArrayList<>();
    private TextView Caption;
    private String _caption;
    private LinearLayout pictures;


    private LinearLayout relativeLayoutCollect;
    private ImageView iv_collect;
    private TextView tv_collect;
    private boolean flag = false;

    private Sqlite userDbHelper = new Sqlite(this);

    private CircleImageView headProtrait;
    TypedValue typedValue = new TypedValue();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);
        Toolbar toolbar = (Toolbar) findViewById(R.id.buy_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);

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
        commentsubmit = (LinearLayout) findViewById(R.id.commentsubmit);
        tv_comm = (TextView) findViewById(R.id.tv_comm);

        headProtrait = (CircleImageView) findViewById(R.id.img_activity_product);
        Caption = (TextView)findViewById(R.id.caption);
        Caption.setText(_caption);
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
                    intent2.putExtra("imgUrl", imgUrl);
                    startActivityForResult(intent2,1);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.scale_fade_out);
                }

            }
        });
        commentsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Buy.this, Comment.class);
                intent.putExtra("itemID",itemID);
                intent.putStringArrayListExtra("comment_texts",comment_texts);
                intent.putStringArrayListExtra("comment_times",comment_times);
                startActivityForResult(intent,2);
                overridePendingTransition(R.anim.slide_in_right, R.anim.scale_fade_out);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = userDbHelper.getWritableDatabase();
                Cursor cursor = db.rawQuery("select * from AXWcollect where itemId = " + itemID + " and userName = '" + GlobalParameterApplication.getUser_name() + "'", null);
                while (cursor.moveToNext()) {
                    flag = true;
                    break;
                }
                cursor.close();
                db.close();
                if(NetInfo.checkNetwork(Buy.this)){
                    GetInfo(itemID);
                    GetComments();
                    HashMap<String,String> usrInfo = ChatList.getUserName("" + OwnerID);
                    ownerName = usrInfo.get("usrName");
                    imgUrl = usrInfo.get("img");
                    picts = Picset.split(",");
                    pic_list.clear();
                    for (int i = 0; i < picts.length; i++){
                        pic_list.add(GlobalParameterApplication.imgSurl+picts[i]);
                    }
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
                        tv_collect.setTextColor(getResources().getColor(typedValue.resourceId));
                        tv_collect.setText("已收藏");
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    Handler nhandler = new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2310231:
                    if(flag){
                        iv_collect.setImageResource(R.drawable.ic_stared);
                        tv_collect.setTextColor(getResources().getColor(typedValue.resourceId));
                        tv_collect.setText("已收藏");
                    }
                    textView1.setText(ownerName);
                    textView2.setText(Desc);
                    textView3.setText("价格：￥"+Price);
                    tv_comm.setText(comment_texts.size()+"留言");
                    if (imgUrl != null && !imgUrl.equals(""))
                        ImageLoader.getInstance().displayImage(GlobalParameterApplication.imgSurl+imgUrl, headProtrait);
                    if(Picset != null && !Picset.equals("")){
                        for (int i = 0; i < pic_list.size(); ++i){
                            ImageView img = new ImageView(Buy.this);
                            img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            LinearLayout.LayoutParams imgLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                            ImageLoader.getInstance().displayImage(pic_list.get(i),img);
                            img.setLayoutParams(imgLayoutParams);
                            pictures.addView(img,imgLayoutParams);
                        }
                    }
                    break;
            }

        }

    };

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

    public void GetComments(){
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
            comment_texts.clear();
            comment_times.clear();
            for (int i = 0; i < result.length();i++){
                outt=result.getJSONObject(i);
                comment_texts.add(outt.getString("content"));
                comment_times.add(outt.getString("created"));
            }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 2 && resultCode == RESULT_OK){
            comment_texts = data.getStringArrayListExtra("comment_texts");
            comment_times = data.getStringArrayListExtra("comment_times");
            tv_comm.setText(comment_texts.size()+"留言");
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.scale_fade_in, R.anim.slide_out_bottom);
        super.onBackPressed();
    }
}

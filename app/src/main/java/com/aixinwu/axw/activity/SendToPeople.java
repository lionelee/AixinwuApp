package com.aixinwu.axw.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aixinwu.axw.R;
import com.aixinwu.axw.adapter.GridImgAdapter;
import com.aixinwu.axw.adapter.PhotoPickAdapter;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.Tool;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class SendToPeople extends Activity{
    public String imageSet;
    private GridView mGridView;
    private Button buttonPublish;
    private EditText doc;
    private String Descrip;
    private ArrayList<String> images = new ArrayList<>();
    private EditText Caption;
    private String _caption;
    private Tool am = new Tool();
    private int HowNew = 1;

    private EditText price;
    private double money;

    private final String surl = GlobalParameterApplication.getSurl();
    public  java.lang.String MyToken;
    private GridImgAdapter adapter;
    private GridImgAdapter.OnClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_send_to_people);
        mGridView = (GridView)findViewById(R.id.gridView1);
        buttonPublish = (Button)findViewById(R.id.button1);
        doc = (EditText)findViewById(R.id.editText1);
        price = (EditText)findViewById(R.id.price);
        Caption = (EditText)findViewById(R.id.commodity_title);
        buttonPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GlobalParameterApplication gpa = (GlobalParameterApplication) getApplicationContext();
                if(gpa.getLogin_status()==0)Toast.makeText(SendToPeople.this,"未登录",Toast.LENGTH_LONG).show();
                else{
                    _caption = Caption.getText().toString();
                    Descrip = doc.getText().toString();
                    HowNew = 1;
                    String now_price = price.getText().toString();
                    if (!now_price.toString().isEmpty()) money = Double.parseDouble(now_price.toString());
                    if(_caption.isEmpty()){
                        Toast.makeText(SendToPeople.this, "商品名称未填写", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (Descrip.isEmpty()){
                        Toast.makeText(SendToPeople.this, "商品描述未填写", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (images.size() == 0) {
                        Toast.makeText(SendToPeople.this, "未选择照片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (price.getText().toString().isEmpty()){
                        Toast.makeText(SendToPeople.this, "商品价格未填写", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final ProgressDialog progressDialog = new ProgressDialog(SendToPeople.this,
                            R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("发布中...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            imageSet = uploadPic();
                            MyToken=GlobalParameterApplication.getToken();
                            String ss= AddItem(HowNew,money,Descrip,imageSet,_caption);
                            Message msg = new Message();
                            msg.what = 133;
                            nHandler.sendMessage(msg);

                        }
                    }).start();
                }

            }
        });

        adapter = new GridImgAdapter(SendToPeople.this);
        listener = new GridImgAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int i) {
                if(i == images.size()){
                    Intent intent = new Intent(SendToPeople.this, PhotoPicker.class);
                    intent.putExtra("mode", PhotoPicker.MULTI_SELECT);
                    intent.putStringArrayListExtra("selectImg",images);
                    startActivityForResult(intent, 0);
                    overridePendingTransition(R.anim.slide_in_right,R.anim.scale_fade_out);
                }else{
                    images.remove(i);
                    adapter.setList(images);
                    adapter.notifyDataSetChanged();
                }
            }
        };
        adapter.setListener(listener);
        mGridView.setAdapter(adapter);
    }

    public Handler nHandler = new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 133:
                    Toast.makeText(SendToPeople.this,"发布成功",Toast.LENGTH_SHORT).show();
                    break;
                case 134:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==0){
            if(resultCode == RESULT_OK) {
                images = data.getStringArrayListExtra("selected");
                adapter.setList(images);
                adapter.notifyDataSetChanged();
            }
        }
    }

    protected String AddItem(int Type, double Money, String Doc, String picstr, String caption){
        String result = null;
        JSONObject matadata = new JSONObject();

        matadata.put("timestamp","12312312213");
        JSONObject iteminfo = new JSONObject();
        iteminfo.put("category",1);
        iteminfo.put("itemCondition",HowNew);
        iteminfo.put("estimatedPriceByUser",Money);
        iteminfo.put("description",Doc);
        iteminfo.put("images",picstr);
        iteminfo.put("caption",caption);
        JSONObject data = new JSONObject();
        data.put("token",MyToken);
        data.put("itemInfo",iteminfo);
        String jsonstr = data.toJSONString();
        try {
            URL url = new URL(surl + "/item_add");
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            conn.setRequestProperty("Content-Type","application/json");
            conn.getOutputStream().write(jsonstr.getBytes());

            String ostr = null;
            ostr = IOUtils.toString(conn.getInputStream());
            org.json.JSONObject outjson = null;
            outjson = new org.json.JSONObject(ostr);
            result = outjson.getJSONObject("itemInfo").getString("ID");
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;

    }
    protected  String uploadPic(){
        String Picset = "";
        for (int i = 1; i < images.size(); i++){
            String ss = images.get(i);
            try {
                String imageID = am.sendFile(surl,ss);
                if (i>1) Picset=Picset+","+imageID;
                else Picset = imageID;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Picset;

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.alpha_fade_in, R.anim.top_to_bottom);
    }

}

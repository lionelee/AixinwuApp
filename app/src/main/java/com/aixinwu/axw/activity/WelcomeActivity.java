package com.aixinwu.axw.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.os.Handler;
import android.util.Log;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.TextView;

import com.aixinwu.axw.database.Sqlite;

import com.aixinwu.axw.R;
import com.aixinwu.axw.tools.DateUtil;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by liangyuding on 2016/4/25.
 */
public class WelcomeActivity extends Activity {

    private long SPLASH_LENGTH;
    Handler handler = new Handler();
    private ImageView iv_welcome;
    private SharedPreferences preferences;

    private Sqlite userDbHelper = new Sqlite(this);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean flag = preferences.getBoolean(getString(R.string.pref_start_img_key),true);
        setContentView(R.layout.activity_welcome);
        if(flag){
            SPLASH_LENGTH = 2300;
            iv_welcome = (ImageView) findViewById(R.id.iv_welcome);
            new ShowImgTask().execute();
        }
        else SPLASH_LENGTH = 0;

        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            GlobalParameterApplication.versionName = info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String , String> versionInfo = getVersionNumber();
                String versionNumber = "";
                if (versionInfo.get("versionCode") != null)
                    versionNumber = versionInfo.get("versionCode");
                int a = versionNumber.compareTo(GlobalParameterApplication.versionName);
                if (versionNumber.compareTo(GlobalParameterApplication.versionName) > 0){
                    GlobalParameterApplication.wetherHaveNewVersion = true;
                }
                else GlobalParameterApplication.wetherHaveNewVersion = false;
            }
        }).start();

        final SQLiteDatabase db = userDbHelper.getReadableDatabase();
        final Cursor cursor = db.rawQuery("select phoneNumber,pwd from AXWuser where userId = 1",null);
        while (cursor.moveToNext()) {
            String phoneNumber = cursor.getString(0); //获取第一列的值,第一列的索引从0开始
            String pwd = cursor.getString(1);//获取第二列的值
            LoginThread loginThread = new LoginThread(phoneNumber,pwd);
            loginThread.start();
            try{
                loginThread.join();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        cursor.close();
        db.close();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivityForResult(intent,0);
                        finish();
                        overridePendingTransition(R.anim.alpha_fade_in,R.anim.alpha_fade_out);
                    }
                });
            }
        }, SPLASH_LENGTH);

    }

    class ShowImgTask extends AsyncTask<Void, Void, Void>{

        private String urlpath = "https://bing.ioliu.cn/v1?w=768&h=1280";
        private File img;

        @Override
        protected Void doInBackground(Void... voids) {
            img = new File(getExternalCacheDir(),"start");
            if(img.exists()){
                int d = preferences.getInt("start_date",0);
                int date = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                if(date > d){
                    img.delete();
                    preferences.edit().putInt("start_date",date).commit();
                }else return null;
            }
            try {
                FileOutputStream fos = new FileOutputStream(img);
                URL url=new URL(urlpath);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                if(conn.getResponseCode() == 200){
                    byte[] buf = new byte[2048];
                    int len = 0;
                    InputStream is = conn.getInputStream();
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(img.exists()){
                ImageLoader.getInstance().displayImage("file://"+img.getAbsolutePath(),iv_welcome);
            }else{
                ImageLoader.getInstance().displayImage(urlpath,iv_welcome);
            }
            super.onPostExecute(aVoid);
        }
    }

    class LoginThread extends Thread{

        private String email,password;
        public LoginThread(String email,String password){
            this.email = email;
            this.password = password;
        }

        @Override
        public void run(){
            try {
                String token = getToken(GlobalParameterApplication.getSurl(), email, password);
                if (token.length() == 0) {

                }
                else {
                    GlobalParameterApplication.setLogin_status(1);
                    GlobalParameterApplication.setToken(token);

                    JSONObject data = new JSONObject();
                    data.put("token",token);
                    URL url=new URL(GlobalParameterApplication.getSurl()+"/usr_get");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(data.toJSONString().getBytes());
                    InputStream input = conn.getInputStream();
                    String ostr = IOUtils.toString(input);
                    org.json.JSONObject outjson = null;
                    outjson = new org.json.JSONObject(ostr);
                    int result = outjson.getJSONObject("userinfo").getInt("ID");
                    String jc = outjson.getJSONObject("userinfo").getString("jaccount");
                    Log.i("LIANGYUDING",jc);
                    if (jc.length() > 0)
                        GlobalParameterApplication.whtherBindJC = 1;
                    else
                        GlobalParameterApplication.whtherBindJC = 0;
                    GlobalParameterApplication.setJaccount(jc);
                    GlobalParameterApplication.setUserID(result);
                    GlobalParameterApplication.start(token);
                    conn.disconnect();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


    public String genJson(String name, String psw) {
        JSONObject matadata = new JSONObject();
        matadata.put("TimeStamp", 123124233);
        matadata.put("Device", "android");
        JSONObject userinfo = new JSONObject();
        userinfo.put("username", name);
        userinfo.put("password", psw);

        JSONObject data = new JSONObject();
        data.put("mataData", matadata);
        data.put("userinfo", userinfo);
        return data.toJSONString();
    }


    public String getToken(String surl, String name, String psw) throws IOException {
        String jsonstr = genJson(name, psw);
        URL url = new URL(surl + "/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(1000);
        conn.setReadTimeout(1000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", String.valueOf(jsonstr.length()));
        conn.getOutputStream().write(jsonstr.getBytes());

        String ostr = IOUtils.toString(conn.getInputStream());
        System.out.println(ostr);

        org.json.JSONObject outjson = null;
        String result = null;
        try {
            outjson = new org.json.JSONObject(ostr);
            result = outjson.getString("token");


        } catch (Exception e) {
            e.printStackTrace();
        }
        conn.disconnect();
        return result;
    }

    public static HashMap<String , String> getVersionNumber(){
        HashMap<String ,String> version = new HashMap<>();

        try {
            URL url = new URL(GlobalParameterApplication.getSurl() + "/version_code");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            java.lang.String ostr ;
            org.json.JSONObject outjson = null;
            if (conn.getResponseCode() == 200){
                ostr = IOUtils.toString(conn.getInputStream());
                org.json.JSONObject result = new org.json.JSONObject(ostr);
                version.put("versionCode",result.getString("version_code"));
                version.put("desp",result.getString("desp"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return version;
    }
}

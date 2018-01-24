package com.aixinwu.axw.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.aixinwu.axw.R;


import butterknife.ButterKnife;
import butterknife.Bind;

import com.aixinwu.axw.database.Sqlite;
import com.aixinwu.axw.tools.GlobalParameterApplication;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


public class LoginActivity extends AppCompatActivity{
    private Sqlite userDbHelper = new Sqlite(this);

    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_login) AppCompatButton _loginButton;
    @Bind(R.id.forgetPWD) Button _forgetPWD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("登录");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _forgetPWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ResetPWD.class);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.slide_in_right,R.anim.scale_fade_out);
            }
        });


        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm.isActive())
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                login();
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
        super.onBackPressed();
    }


    public void login() {
        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("验证中...");
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        LoginThread loginThread = new LoginThread(email,password);

        loginThread.start();

        try{
            loginThread.join();
        }catch (Exception e){
            e.printStackTrace();
        }

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        int login_status = GlobalParameterApplication.getLogin_status();
                        if (login_status == 1)
                            onLoginSuccess();
                        else
                            onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);

    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
        overridePendingTransition(R.anim.scale_fade_in, R.anim.slide_out_bottom);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "登录失败", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty()) {
            _emailText.setError("请输入手机号");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty()) {
            _passwordText.setError("请输入密码");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    class LoginThread extends Thread{

        private String email,password;
        public LoginThread(String email,String password){
            this.email = email;
            this.password = password;
            SQLiteDatabase db = userDbHelper.getWritableDatabase();
            db.execSQL("delete from AXWuser where userId = 1");
            db.execSQL("insert into AXWuser(userId,phoneNumber,pwd) values(1,'"+email+"','"+password+"')");
            db.close();
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
                    Log.i("LIANGYUDING", jc);
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




}

package com.aixinwu.axw.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aixinwu.axw.adapter.NotifyMessage;

import com.aixinwu.axw.R;
import com.aixinwu.axw.adapter.PagerAdapter;
import com.aixinwu.axw.database.Sqlite;
import com.aixinwu.axw.fragment.HomePage;
import com.aixinwu.axw.fragment.ShoppingCart;
import com.aixinwu.axw.fragment.UsedDeal;
import com.aixinwu.axw.tools.DownloadTask;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by liangyuding on 2016/4/6.
 * Modified by lionel on 2017/10/15
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener{
    private String coins="", username="", headProtrait="", updateDesp ="";;
    private Intent intent=null;
 
    private int mBackKeyPressedTimes = 0;
    public static Activity mActivity;

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private CircleImageView iv_avatar;
    private TextView tv_coins, tv_uname, tv_type;
    private AppCompatButton btn_login, btn_register, btn_sign;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mActivity = MainActivity.this;
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initView();
        try{
            NotifyThread.start();
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    private void initView(){
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nav_view = navigationView.getHeaderView(0);
        iv_avatar = (CircleImageView) nav_view.findViewById(R.id.nav_avatar);
        iv_avatar.setOnClickListener(this);
        tv_coins = (TextView)nav_view.findViewById(R.id.nav_coins);
        tv_uname = (TextView)nav_view.findViewById(R.id.nav_uname);
        tv_type = (TextView)nav_view.findViewById(R.id.nav_type);
        btn_login = (AppCompatButton) nav_view.findViewById(R.id.nav_login);
        btn_login.setOnClickListener(this);
        btn_register = (AppCompatButton) nav_view.findViewById(R.id.nav_register);
        btn_register.setOnClickListener(this);
        btn_sign = (AppCompatButton) nav_view.findViewById(R.id.nav_sign);
        btn_sign.setOnClickListener(this);

        tabLayout = (TabLayout) findViewById(R.id.tab_header);
        viewPager = (ViewPager) findViewById(R.id.main_viewpager);
        String[] strings = new String[]{getString(R.string.homepage),getString(R.string.usedDeal),getString(R.string.shopCart)};
        PagerAdapter adapter = new PagerAdapter(MainActivity.this, getSupportFragmentManager(),strings);
        adapter.addItem(new HomePage());
        adapter.addItem(new UsedDeal());
        adapter.addItem(new ShoppingCart());

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);

    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if(intent != null){
            String msg = intent.getStringExtra("msg");
            if(msg != null && msg.equals("cart")){
            }
        }

        if (GlobalParameterApplication.getLogin_status() == 1) {
            tv_uname.setVisibility(View.VISIBLE);
            tv_coins.setVisibility(View.VISIBLE);
            tv_type.setVisibility(View.VISIBLE);
            btn_sign.setVisibility(View.VISIBLE);
            btn_login.setVisibility(View.GONE);
            btn_register.setVisibility(View.GONE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getPersonalInfo();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Message msg = new Message();
                    msg.what=123123;
                    mHandler.sendMessage(msg);
                }
            }).start();
            navigationView.getMenu().findItem(R.id.nav_exit).setVisible(true);
        }
        else if (GlobalParameterApplication.getLogin_status() == 0) {
            setOffStatus();
        }
    }

    private void setOffStatus(){
        tv_uname.setVisibility(View.INVISIBLE);
        tv_coins.setVisibility(View.GONE);
        tv_type.setVisibility(View.INVISIBLE);
        btn_sign.setVisibility(View.GONE);
        btn_login.setVisibility(View.VISIBLE);
        btn_register.setVisibility(View.VISIBLE);
        iv_avatar.setImageResource(R.color.gray);
        navigationView.getMenu().findItem(R.id.nav_exit).setVisible(false);
    }

    public String genJson(String token) {
        JSONObject matadata = new JSONObject();
        matadata.put("TimeStamp", 123124233);
        matadata.put("Device", "android");
        JSONObject data = new JSONObject();
        data.put("mataData", matadata);
        data.put("token", token);
        return data.toJSONString();
    }

    private void getPersonalInfo () throws IOException {
        String token = GlobalParameterApplication.getToken();
        String jsonstr = genJson(token);
        String surl = GlobalParameterApplication.getSurl();
        URL url = new URL(surl + "/usr_get");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(1000);
        conn.setReadTimeout(1000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", String.valueOf(jsonstr.length()));
        conn.getOutputStream().write(jsonstr.getBytes());

        String ostr = IOUtils.toString(conn.getInputStream());
        org.json.JSONObject outjson;
        org.json.JSONObject userinfojson;
        String userinfo;
        try {
            outjson = new org.json.JSONObject(ostr);
            userinfo = outjson.getString("userinfo");
            userinfojson = new org.json.JSONObject(userinfo);
            System.out.println("HEELO\n"+userinfojson.toString());
            coins = userinfojson.getString("coins");
            String myUserName = userinfojson.getString("username");
            String myNickName = userinfojson.getString("nickname");
            if (myNickName.length() == 0)
                username = myUserName;
            else username = myNickName;
            headProtrait = userinfojson.getString("image");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                Intent intent = new Intent(MainActivity.this, Search.class);
                startActivityForResult(intent,0);
                overridePendingTransition(R.anim.alpha_fade_in,R.anim.alpha_fade_out);
                break;
            case R.id.action_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareApp));
                startActivity(Intent.createChooser(shareIntent, getString(R.string.shareApp_choser_label)));
                break;
            default:break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        intent = null;
        switch (item.getItemId()) {
            case R.id.nav_profile:
                if (GlobalParameterApplication.getLogin_status() == 1){
                    startAct(PersonalCenter.class);
                } else {
                    Toast.makeText(this,"请先登录",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_coin:
                if (GlobalParameterApplication.getLogin_status() == 1){
                    intent = new Intent(MainActivity.this, CashTransfer.class);
                } else {
                    Toast.makeText(this,"请先登录",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_msg:
                if (GlobalParameterApplication.getLogin_status() == 1){
                    intent = new Intent(MainActivity.this, ChatList.class);
                } else {
                    Toast.makeText(this,"请先登录",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_exit:
                logOff();
                break;
            case R.id.nav_settings:
                intent = new Intent(MainActivity.this, Settings.class);
                break;
            case R.id.nav_help:
                intent = new Intent(MainActivity.this, HelpFeed.class);
                break;
            case R.id.nav_about:
                intent = new Intent(MainActivity.this, AXWInfo.class);
                break;
            default:break;
        }
        if(intent != null){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivityForResult(intent,0);
                    overridePendingTransition(R.anim.slide_in_right,R.anim.scale_fade_out);
                }
            },140);
        }
        item.setChecked(true);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logOff(){
        if (GlobalParameterApplication.getLogin_status() == 0){
            Toast.makeText(this,"请先登录",Toast.LENGTH_SHORT).show();
            return;
        }
        new  AlertDialog.Builder(MainActivity.this)
                .setTitle("提示" )
                .setMessage("确定要退出当前账号？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int ii) {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run() {
                                try{
                                    SQLiteDatabase db = new Sqlite(MainActivity.this).getWritableDatabase();
                                    db.execSQL("delete from AXWuser where userId = 1");
                                    db.close();}
                                catch(Throwable e){
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                        GlobalParameterApplication.setToken("");
                        GlobalParameterApplication.setLogin_status(0);
                        GlobalParameterApplication.stop();
                        setOffStatus();
                    }
                })
                .setNegativeButton("取消",null).show();
    }

    private void startAct(final Class cls){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, cls);
                intent.putExtra("uname", username);
                intent.putExtra("headProtrait",headProtrait);
                startActivityForResult(intent,0);
                overridePendingTransition(R.anim.slide_in_right,R.anim.scale_fade_out);
            }
        },140);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.nav_avatar:
                if (GlobalParameterApplication.getLogin_status() == 1){
                    startAct(PersonalCenter.class);
                } else {
                    Toast.makeText(this,"请先登录",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_login:
                startAct(LoginActivity.class);
                break;
            case R.id.nav_register:
                startAct(SignupActivity.class);
                break;
            case R.id.nav_sign:
                break;
            default:break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mBackKeyPressedTimes == 0) {
                Toast.makeText(this, getResources().getString(R.string.pressAgain), Toast.LENGTH_SHORT).show();
                mBackKeyPressedTimes = 1;
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            mBackKeyPressedTimes = 0;
                        }
                    }
                }.start();
                return;
            }
            else{
                finish();
                System.exit(0);
            }
            super.onBackPressed();
        }
    }

    public Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 123112:
                    NotifyMessage st = (NotifyMessage) msg.getData().getSerializable("now");

                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    Intent intent = new Intent(getApplicationContext(), ChatList.class);
                    PendingIntent pendingIntent2 = PendingIntent.getActivity(getApplicationContext(), GlobalParameterApplication.notifyid,
                            intent, 0);
                    Notification notify2 = new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.aixinwu) // 设置状态栏中的小图片，尺寸一般建议在24×24，这个图片同样也是在下拉状态栏中所显示，如果在那里需要更换更大的图片，可以使用setLargeIcon(Bitmap
                            // icon)
                            .setTicker( "您有来自用户"+st.getWho()+"的新消息，请注意查收")// 设置在status
                            // bar上显示的提示文字
                            .setContentTitle("来自用户"+st.getWho()+"的新消息")// 设置在下拉status
                            // bar后Activity，本例子中的NotififyMessage的TextView中显示的标题
                            .setContentText("点击打开聊天列表")// TextView中显示的详细内容
                            .setContentIntent(pendingIntent2) // 关联PendingIntent
                            .setNumber(1) // 在TextView的右方显示的数字，可放大图片看，在最右侧。这个number同时也起到一个序列号的左右，如果多个触发多个通知（同一ID），可以指定显示哪一个。
                            .getNotification(); // 需要注意build()是在API level
                    // 16及之后增加的，在API11中可以使用getNotificatin()来代替
                    notify2.flags |= Notification.FLAG_AUTO_CANCEL;
                    notify2.defaults = Notification.DEFAULT_SOUND;
                    manager.notify(GlobalParameterApplication.notifyid, notify2);
                    GlobalParameterApplication.notifyid++;
                    break;
                case 123123:
                    tv_coins.setText("爱心币： " + coins);
                    if (GlobalParameterApplication.whtherBindJC == 1)
                        tv_type.setText("校园用户");
                    else tv_type.setText("社区用户");
                    tv_uname.setText(username);
                    if (headProtrait!=null && headProtrait.length() != 0)
                        ImageLoader.getInstance().displayImage(GlobalParameterApplication.imgSurl+headProtrait, iv_avatar);
                    break;
                case 395923:
                    if (GlobalParameterApplication.wetherHaveNewVersion){
                        new  AlertDialog.Builder(MainActivity.this)
                                .setTitle("爱心屋APP下载" )
                                .setMessage("搜索到有新版本，是否下载？\n"+updateDesp)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int ii) {
                                        if (PackageManager.PERMISSION_GRANTED ==
                                                ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE)){
                                            DownloadTask downloadTask = new DownloadTask(MainActivity.this);
                                            downloadTask.execute("http://salary.aixinwu.info/apk/axw.apk");
                                        } else {
                                            if(Build.VERSION.SDK_INT >= 23) {
                                                ActivityCompat.requestPermissions(MainActivity.this,new String[]{WRITE_EXTERNAL_STORAGE}, 5698);
                                            }
                                        }
                                    }
                                })
                                .setNegativeButton("取消",null).show();
                    }
                    else{
                        new  AlertDialog.Builder(MainActivity.this)
                                .setTitle("爱心屋APP下载" )
                                .setMessage("当前已经是最新版本" )
                                .setPositiveButton("确定", null).show();
                    }
                default: break;
            }
        }
    };
    public Thread NotifyThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true){
                if (GlobalParameterApplication.getLogin_status() == 1){
                    NotifyMessage now = GlobalParameterApplication.sentMessages.peek();
                    if (now != null){
                        GlobalParameterApplication.sentMessages.remove();
                        Message msg = new Message();
                        msg.what = 123112;
                        Bundle b = new Bundle();
                        b.putSerializable("now", now);
                        msg.setData(b);
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }
    });

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 5698){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                DownloadTask downloadTask = new DownloadTask(MainActivity.this);
                downloadTask.execute("http://salary.aixinwu.info/apk/axw.apk");
            } else {
                Toast.makeText(MainActivity.this,getString(R.string.permission_denied_info),Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }
}

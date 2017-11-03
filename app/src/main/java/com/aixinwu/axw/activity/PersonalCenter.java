package com.aixinwu.axw.activity;

//import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
//import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aixinwu.axw.Adapter.PagerAdapter;
import com.aixinwu.axw.R;
import com.aixinwu.axw.database.Sqlite;
import com.aixinwu.axw.fragment.ItemList;
import com.aixinwu.axw.fragment.ItemRecord;
import com.aixinwu.axw.fragment.MyDonation;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by lionel on 2017/10/19.
 */
public class PersonalCenter extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_center);

        Toolbar toolbar = (Toolbar) findViewById(R.id.personal_toolbar);
        setSupportActionBar(toolbar);
        String uname = getIntent().getStringExtra("uname");
        if(!TextUtils.isEmpty(uname))
            getSupportActionBar().setTitle(uname);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout = (TabLayout) findViewById(R.id.tab_header);
        viewPager = (ViewPager) findViewById(R.id.tab_container);
        ImageView iv_avatar = (ImageView) findViewById(R.id.personal_avatar);
        String headProtrait = getIntent().getStringExtra("headProtrait");
        if(!TextUtils.isEmpty(headProtrait))
            ImageLoader.getInstance().displayImage(GlobalParameterApplication.imgSurl+headProtrait, iv_avatar);

        PagerAdapter adapter = new PagerAdapter(PersonalCenter.this, getSupportFragmentManager());
        adapter.addItem(new ItemRecord());
        adapter.addItem(new ItemList());
        adapter.addItem(new MyDonation());

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_personal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.personal_edit:
                break;
            case R.id.personal_exit:
                logOff();
                break;
            case R.id.personal_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareApp));
                startActivity(Intent.createChooser(shareIntent, getString(R.string.shareApp_choser_label)));
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PersonalCenter.this,MainActivity.class));
        overridePendingTransition(R.anim.scale_fade_in,R.anim.slide_out_right);
        finish();
        super.onBackPressed();
    }

    private void logOff(){
        new  AlertDialog.Builder(PersonalCenter.this)
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
                                    SQLiteDatabase db = new Sqlite(PersonalCenter.this).getWritableDatabase();
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
                        onBackPressed();
                    }
                })
                .setNegativeButton("取消",null).show();
    }
}

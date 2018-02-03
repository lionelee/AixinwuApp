package com.aixinwu.axw.activity;

//import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
//import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.aixinwu.axw.adapter.PagerAdapter;
import com.aixinwu.axw.R;
import com.aixinwu.axw.database.Sqlite;
import com.aixinwu.axw.fragment.MyIssue;
import com.aixinwu.axw.fragment.MyBought;
import com.aixinwu.axw.fragment.MyCollection;
import com.aixinwu.axw.fragment.MyDonation;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by lionel on 2017/10/19.
 */
public class PersonalCenter extends AppCompatActivity {
    private CollapsingToolbarLayout layout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView iv_avatar;
    private String headProtrait, uname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_center);

        Toolbar toolbar = (Toolbar) findViewById(R.id.personal_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        uname = getIntent().getStringExtra("uname");
        layout = (CollapsingToolbarLayout)findViewById(R.id.personal_layout);
        if(!TextUtils.isEmpty(uname))
            layout.setTitle(uname);

        tabLayout = (TabLayout) findViewById(R.id.tab_header);
        viewPager = (ViewPager) findViewById(R.id.tab_container);
        iv_avatar = (ImageView) findViewById(R.id.personal_avatar);
        headProtrait = getIntent().getStringExtra("headProtrait");
        if(!TextUtils.isEmpty(headProtrait))
            ImageLoader.getInstance().displayImage(GlobalParameterApplication.imgSurl+headProtrait, iv_avatar);

        String[] strings = new String[]{getString(R.string.tab0),getString(R.string.tab1), getString(R.string.tab2),getString(R.string.tab3)};
        PagerAdapter adapter = new PagerAdapter(PersonalCenter.this, getSupportFragmentManager(),strings);
        adapter.addItem(new MyBought());
        adapter.addItem(new MyDonation());
        adapter.addItem(new MyIssue());
        adapter.addItem(new MyCollection());

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_personal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.personal_edit:
                Intent intent = new Intent(PersonalCenter.this, ModifyProfile.class);
                intent.putExtra("uname", uname);
                intent.putExtra("headProtrait",headProtrait);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.slide_in_right,R.anim.scale_fade_out);
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
        finish();
        overridePendingTransition(R.anim.scale_fade_in, R.anim.slide_out_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode == RESULT_OK){
            if(!data.getStringExtra("newavatar").equals(headProtrait)){
                headProtrait = data.getStringExtra("newavatar");
                ImageLoader.getInstance().displayImage(GlobalParameterApplication.imgSurl+headProtrait, iv_avatar);
            }
            uname = data.getStringExtra("newuname");
            layout.setTitle(uname);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void logOff(){
        new  AlertDialog.Builder(PersonalCenter.this)
                .setTitle("注销" )
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
                                    db.close();
                                } catch(Throwable e){
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

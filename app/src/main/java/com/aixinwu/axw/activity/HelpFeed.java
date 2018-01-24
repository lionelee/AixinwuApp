package com.aixinwu.axw.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.aixinwu.axw.R;

/**
 * Created by lionel on 2017/11/10.
 */

public class HelpFeed extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_feed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.help_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("帮助与反馈");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    }
}

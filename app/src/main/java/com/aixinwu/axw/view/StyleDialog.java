package com.aixinwu.axw.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.aixinwu.axw.R;
import com.aixinwu.axw.adapter.StyleListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 10911 on 2018/1/25.
 */

public class StyleDialog extends BottomSheetDialog {

    private Activity mActivity;
    private SharedPreferences mSharedPreferences;
    private ListView lv_style;
    private StyleListAdapter adapter;
    String values[];

    public StyleDialog(@NonNull final Activity activity, SharedPreferences sharedPreferences) {
        super(activity);
        mActivity = activity;
        values =  mActivity.getResources().getStringArray(R.array.pref_theme_option_values);
        mSharedPreferences = sharedPreferences;
        String str = mSharedPreferences.getString(mActivity.getString(R.string.pref_theme_key), mActivity.getString(R.string.pref_theme_default));
        int select = Integer.parseInt(str);

        View v = getLayoutInflater().inflate(R.layout.bottom_dialog_style,null);
        setContentView(v);
        lv_style = (ListView) v.findViewById(R.id.style_list);
        adapter = new StyleListAdapter(mActivity, select);
        lv_style.setAdapter(adapter);
        lv_style.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.chgSelect(i);
                mSharedPreferences.edit().putString(mActivity.getString(R.string.pref_theme_key),values[i]).commit();
                dismiss();
            }
        });
    }
}

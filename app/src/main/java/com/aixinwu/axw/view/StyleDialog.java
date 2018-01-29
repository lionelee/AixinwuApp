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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.aixinwu.axw.R;

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
    int colors[] ={R.color.primary, R.color.SeieeBlue, R.color.UltraViolet, R.color.ChiliOil,
            R.color.LittleBoyBlue, R.color.Arcadia, R.color.Emperador};
    String names[];
    String values[];
    private List<HashMap<String, Object>> lists = new ArrayList<>();

    public StyleDialog(@NonNull final Activity activity) {
        super(activity);
        mActivity = activity;
        names =  mActivity.getResources().getStringArray(R.array.pref_theme_option_labels);
        values =  mActivity.getResources().getStringArray(R.array.pref_theme_option_values);
        for(int i = 0; i < colors.length; ++i){
            HashMap<String, Object> map = new HashMap();
            map.put("styleColor",colors[i]);
            map.put("styleName", names[i]);
            lists.add(map);
        }

        View v = getLayoutInflater().inflate(R.layout.bottom_dialog_style,null);
        setContentView(v);
        lv_style = (ListView) v.findViewById(R.id.style_list);
        SimpleAdapter simpleAdapter = new SimpleAdapter(mActivity,lists,R.layout.item_style_list,
                new String[]{"styleColor","styleName"},new int[]{R.id.style_color,R.id.style_name});
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object o, String s) {
                if(view instanceof ImageView){
                    ((ImageView)view).setImageResource((Integer)o);
                    return true;
                }else if(view instanceof TextView){
                    ((TextView)view).setText((String)o);
                    ((TextView)view).setTextColor(getColor(o));
                    return true;
                }
                return false;
            }
        });
        lv_style.setAdapter(simpleAdapter);
        lv_style.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSharedPreferences.edit().putString(mActivity.getString(R.string.pref_theme_key),values[i]).commit();
dismiss();
            }
        });
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences){
        mSharedPreferences = sharedPreferences;
    }

    public int getColor(Object o){
        String name = (String)o;
        for(int i = 0; i < names.length; ++i){
            if(names[i].equals(name)){
                return mActivity.getResources().getColor(colors[i]);
            }
        }
        return -1;
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        int screenHeight = getScreenHeight(mActivity);
//        int statusBarHeight = getStatusBarHeight(getContext());
//        int dialogHeight = screenHeight - statusBarHeight;
//        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight == 0 ? ViewGroup.LayoutParams.MATCH_PARENT : dialogHeight);
//    }

    private static int getScreenHeight(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    private static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }
}

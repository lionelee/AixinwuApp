package com.aixinwu.axw.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aixinwu.axw.R;
import com.aixinwu.axw.activity.MainActivity;
import com.aixinwu.axw.activity.Settings;
import com.aixinwu.axw.activity.WelcomeActivity;
import com.aixinwu.axw.tools.DownloadTask;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.NetInfo;
import com.aixinwu.axw.tools.Tool;
import com.aixinwu.axw.view.StyleDialog;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by lionel on 2017/11/15.
 */

public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private String updateDesp ="";
    private SharedPreferences sharedPreferences;
    private StyleDialog dialog;
    String labels[];
    String values[];
    int colors[] ={R.color.primary, R.color.SeieeBlue, R.color.UltraViolet, R.color.ChiliOil,
            R.color.LittleBoyBlue, R.color.Arcadia, R.color.Emperador};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);

        sharedPreferences = getPreferenceScreen().getSharedPreferences();
        dialog = new StyleDialog(getActivity());
        dialog.setSharedPreferences(sharedPreferences);
        PreferenceCategory pc = (PreferenceCategory)getPreferenceScreen().getPreference(0);
        int count = pc.getPreferenceCount();
        for(int i = 0; i < count; ++i){
            Preference p = pc.getPreference(i);
            if (!(p instanceof SwitchPreference)){
                String key = p.getKey();
                String value = sharedPreferences.getString(p.getKey(), "");
                if(value.equals("")){
                    if(key.equals(getActivity().getString(R.string.pref_size_key))){
                        value = getActivity().getString(R.string.pref_size_default);
                    }else if(key.equals(getActivity().getString(R.string.pref_display_key))){
                        value = getActivity().getString(R.string.pref_display_default);
                    }
                }
                setPreferenceSummary(p, value);
            }
        }
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        Preference theme = findPreference(getString(R.string.pref_theme_key));
        String value = sharedPreferences.getString(theme.getKey(),"");
        labels = getResources().getStringArray(R.array.pref_theme_option_labels);
        values = getResources().getStringArray(R.array.pref_theme_option_values);
        if(value.equals("")){
            theme.setSummary(getString(R.string.pref_theme_label_ag));
        }else {
            for(int i = 0; i < values.length; ++i){
                if(values[i].equals(value)){
                    theme.setSummary(labels[i]);
                    break;
                }
            }
        }
        theme.setOnPreferenceClickListener(this);
        Preference bindJA = findPreference(getString(R.string.pref_bind_JAccount_key));
        bindJA.setOnPreferenceClickListener(this);
        Preference checkUP = findPreference(getString(R.string.pref_check_update_key));
        checkUP.setOnPreferenceClickListener(this);
        if(GlobalParameterApplication.wetherHaveNewVersion)
            checkUP.setWidgetLayoutResource(R.layout.badge);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference != null) {
            if (!(preference instanceof SwitchPreference)) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                if(key.equals(getString(R.string.pref_theme_key))){
                    for(int i = 0; i < values.length; ++i){
                        if(values[i].equals(value)){
                            preference.setSummary(labels[i]);
                            GlobalParameterApplication.setThemeId(i);
                            if(Build.VERSION.SDK_INT < 21){
                                getActivity().recreate();
                            }else{
                                int color = getResources().getColor(colors[i]);
                                getActivity().getWindow().setStatusBarColor(color);
                                ((Settings)getActivity()).toolbar.setBackgroundColor(color);
                            }
                            ((Settings)getActivity()).flag = true;
                            break;
                        }
                    }
                }else
                    setPreferenceSummary(preference, value);
            }
        }
    }


    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if(key.equals(getString(R.string.pref_bind_JAccount_key))){
            bindJaccount();
        }
        else if(key.equals(getString(R.string.pref_check_update_key))){
            checkUpdate();
        }else if(key.equals(getString(R.string.pref_theme_key))){
            dialog.show();
        }
        return false;
    }

    private void bindJaccount(){
        if (GlobalParameterApplication.getLogin_status() != 1){
            Toast.makeText(getActivity(),"请先登录",Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int status = -1;
                String MyToken= GlobalParameterApplication.getToken();
                String surl = GlobalParameterApplication.getSurl();
                JSONObject orderrequest = new JSONObject();
                orderrequest.put("token", MyToken);
                try {
                    URL url = new URL(surl + "/aixinwu_associate_jaccount");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(orderrequest.toJSONString().getBytes());
                    java.lang.String ostr = IOUtils.toString(conn.getInputStream());
                    org.json.JSONObject outjson = new org.json.JSONObject(ostr);
                    status = outjson.getJSONObject("status").getInt("code");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String msg = "绑定失败";
                if (status == 0)
                    msg = "绑定成功";
                final String finalMsg = msg;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), finalMsg,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 395923:
                    if (GlobalParameterApplication.wetherHaveNewVersion){
                        new  AlertDialog.Builder(getActivity())
                                .setTitle("爱心屋APP下载" )
                                .setMessage("检测到有新版本，是否下载？\n"+updateDesp)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int ii) {
                                        checkPermission();
                                    }
                                })
                                .setNegativeButton("取消",null).show();
                    }
                    else{
                        Toast.makeText(getActivity(),"已是最新版本",Toast.LENGTH_SHORT).show();
                    }
                default:break;
            }
        }
    };

    private void checkUpdate(){
        if(!NetInfo.checkNetwork(getActivity())){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String , String> versionInfo = WelcomeActivity.getVersionNumber();
                String versionNumber = versionInfo.get("versionCode");
                updateDesp = versionInfo.get("desp");
                int a = versionNumber.compareTo(GlobalParameterApplication.versionName);
                if (versionNumber.compareTo(GlobalParameterApplication.versionName) > 0){
                    GlobalParameterApplication.wetherHaveNewVersion = true;
                }
                else GlobalParameterApplication.wetherHaveNewVersion = false;

                Message msg = new Message();
                msg.what = 395923;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    private void checkPermission(){
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE)){
            DownloadTask downloadTask = new DownloadTask(getActivity());
            downloadTask.execute("http://salary.aixinwu.info/apk/axw.apk");
        } else {
            if(Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(getActivity(),new String[]{WRITE_EXTERNAL_STORAGE}, 5698);
            }
        }
    }
}

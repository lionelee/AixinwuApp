package com.aixinwu.axw.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.aixinwu.axw.R;
import com.aixinwu.axw.tools.DownloadTask;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.NetInfo;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by lionel on 2017/11/15.
 */

public class HelpFeedFragment extends PreferenceFragment{

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.help_feedback);
    }
}

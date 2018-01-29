package com.aixinwu.axw.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.aixinwu.axw.R;
import com.aixinwu.axw.activity.FeedBack;
import com.aixinwu.axw.activity.SignupBind;

/**
 * Created by lionel on 2017/11/15.
 */

public class HelpFeedFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener{

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.help_feedback);

        Preference feedback = findPreference(getString(R.string.pref_feedback_key));
        Preference rbindJA = findPreference(getString(R.string.pref_rabj_key));
        feedback.setOnPreferenceClickListener(this);
        rbindJA.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        Intent intent = null;
        if(key.equals(getString(R.string.pref_feedback_key))){
            intent = new Intent(getActivity(), FeedBack.class);
        }
        else if(key.equals(getString(R.string.pref_rabj_key))){
            intent = new Intent(getActivity(), SignupBind.class);
            intent.putExtra("url","https://mp.weixin.qq.com/s/jLzyrkCv9ZbowaX_GFDnoA");
        }
        if(intent != null){
            startActivityForResult(intent,0);
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.scale_fade_out);
        }
        return false;
    }
}

package com.aixinwu.axw.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


import com.aixinwu.axw.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lionel on 2017/10/19.
 */

public class PagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private List<Fragment> fragments = new ArrayList<>();
    private List<String> titles = new ArrayList<>();

    public PagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        titles.add(context.getString(R.string.tab0));
        titles.add(context.getString(R.string.tab1));
        titles.add(context.getString(R.string.tab2));
    }

    public void addItem(Fragment frag) {
        fragments.add(frag);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }



}

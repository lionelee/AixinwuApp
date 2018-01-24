package com.aixinwu.axw.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aixinwu.axw.R;

/**
 * Created by lionel on 2018/1/21.
 */

public class LaunchDialog extends BottomSheetDialog {

    private Activity mActivity;

    public LaunchDialog(@NonNull Activity activity) {
        super(activity);
        mActivity = activity;
        View v = getLayoutInflater().inflate(R.layout.bottom_launch,null);
        setContentView(v);
        ImageView iv_deal = (ImageView) v.findViewById(R.id.deal);
        ImageView iv_donate = (ImageView) v.findViewById(R.id.donate);

        iv_deal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SendToPeople.class);
                mActivity.startActivityForResult(intent,0);
                mActivity.overridePendingTransition(R.anim.bottom_to_top,R.anim.alpha_fade_out);
                dismiss();
            }
        });

        iv_donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SendToAXW.class);
                mActivity.startActivityForResult(intent,0);
                mActivity.overridePendingTransition(R.anim.bottom_to_top,R.anim.alpha_fade_out);
                dismiss();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int screenHeight = getScreenHeight(mActivity);
        int statusBarHeight = getStatusBarHeight(getContext());
        int dialogHeight = screenHeight - statusBarHeight;
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight == 0 ? ViewGroup.LayoutParams.MATCH_PARENT : dialogHeight);
    }

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

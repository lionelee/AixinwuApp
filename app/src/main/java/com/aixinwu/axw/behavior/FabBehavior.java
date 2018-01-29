package com.aixinwu.axw.behavior;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.aixinwu.axw.view.MyRefreshLayout;

import java.util.List;

/**
 * Created by lionel on 2017/10/15.
 */

public class FabBehavior extends CoordinatorLayout.Behavior<View> {

    private boolean visible = true;

    public FabBehavior(){
        super();
    }

    public FabBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //拦截Y轴上的滑动
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {

        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
//       return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild,
//                target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (dyConsumed > 0 && visible) {
            visible = false;
            onHide(child);
        } else if (dyConsumed < 0) {
            visible = true;
            onShow(child);
        }
    }

    public void onHide(View view) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        view.animate().translationY(view.getHeight() + layoutParams.bottomMargin).setInterpolator(new AccelerateInterpolator());
    }

    public void onShow(View view) {
        view.animate().translationY(0).setInterpolator(new DecelerateInterpolator(3));
    }

}
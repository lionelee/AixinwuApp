package com.aixinwu.axw.view;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorUpdateListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.aixinwu.axw.R;

/**
 * Created by lionel on 2018/1/22.
 */

public class MyRefreshLayout extends SwipeRefreshLayout {

    private View mChildView;
    private MyFooterView footerView;
    private boolean mLoading = false;
    private float mTouchY;
    private float mCurrentY;
    private float mFootHeight;
    private float mPullHeight;

    private DecelerateInterpolator decelerateInterpolator;
    private OnLoadingListener mLoadingListener;

    public MyRefreshLayout(Context context) {
        super(context);
    }

    public MyRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        decelerateInterpolator = new DecelerateInterpolator(10);
        for(int i = 0; i < getChildCount(); ++i){
            View v = getChildAt(i);
            if(v instanceof RecyclerView){
                mChildView = v;
                break;
            }
        }
        if (mChildView == null) {
            return;
        }
        Context context = getContext();
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, tv, true);
        setColorSchemeResources(tv.resourceId);
        footerView = new MyFooterView(context);
        footerView.setVisibility(GONE);
        addView(footerView);
        mFootHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,64,getResources().getDisplayMetrics());
        mPullHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,128,getResources().getDisplayMetrics());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mLoading) return true;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchY = ev.getY();
                mCurrentY = mTouchY;
                break;
            case MotionEvent.ACTION_MOVE:
                float currentY = ev.getY();
                float dy = currentY - mTouchY;
                if (dy < 0 && !canChildScrollDown()) {
                    if (footerView != null) {
                        footerView.onBegin();
                    }
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mLoading) return super.onTouchEvent(e);
        mCurrentY = e.getY();
        float dy = mCurrentY - mTouchY;
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if(dy < 0){
                    dy = Math.abs(dy);
                    dy = Math.min(mPullHeight, dy);
                    if (mChildView != null) {
                        float offsetY = decelerateInterpolator.getInterpolation(dy / mPullHeight) * dy;
                        if (footerView != null) {
                            footerView.getLayoutParams().height = (int) offsetY;
                            footerView.requestLayout();
                        }
                        ViewCompat.setTranslationY(mChildView, -offsetY);
                    }
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mChildView != null) {
                    mCurrentY = e.getY();
                    if (dy < 0 && footerView != null) {
                        if (ViewCompat.getTranslationY(mChildView) <= -mFootHeight) {
                            mLoading = true;
                            createAnimatorTranslationY(mChildView, -mFootHeight, footerView);
                            mLoadingListener.onLoad();
                        } else {
                            mLoading = false;
                            createAnimatorTranslationY(mChildView, 0, footerView);
                        }
                    }
                }
                return true;

        }

        return super.onTouchEvent(e);
    }

    public boolean canChildScrollDown() {
        if (mChildView == null) return false;
        return ViewCompat.canScrollVertically(mChildView, 1);
    }

    public void createAnimatorTranslationY(final View v, final float h, final View fl) {
        ViewPropertyAnimatorCompat viewPropertyAnimatorCompat = ViewCompat.animate(v);
        viewPropertyAnimatorCompat.setDuration(250);
        viewPropertyAnimatorCompat.setInterpolator(new DecelerateInterpolator());
        viewPropertyAnimatorCompat.translationY(h);
        viewPropertyAnimatorCompat.start();
        viewPropertyAnimatorCompat.setUpdateListener(new ViewPropertyAnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(View view) {
                float height = ViewCompat.getTranslationY(v);
                fl.getLayoutParams().height = (int) height;
                fl.requestLayout();
            }
        });
    }

    public void setOnLoadListener(OnLoadingListener listener){
        mLoadingListener = listener;
    }

    public void setLoading(boolean loading){
        mLoading = loading;
        if(loading){
            //TODO
        }else{
            if (mChildView != null) {
                ViewPropertyAnimatorCompat viewPropertyAnimatorCompat = ViewCompat.animate(mChildView);
                viewPropertyAnimatorCompat.setDuration(200);
                viewPropertyAnimatorCompat.y(ViewCompat.getTranslationY(mChildView));
                viewPropertyAnimatorCompat.translationY(0);
                viewPropertyAnimatorCompat.setInterpolator(new DecelerateInterpolator());
                viewPropertyAnimatorCompat.start();
                if (footerView != null) {
                    footerView.onFinish();
                }
            }
        }
    }

    public interface OnLoadingListener{
        public void onLoad();
    }
}

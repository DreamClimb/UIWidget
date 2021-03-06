package com.aries.ui.helper.status;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.aries.ui.util.FindViewUtil;
import com.aries.ui.view.title.TitleBarView;
import com.aries.ui.widget.R;

import java.lang.ref.SoftReference;

/**
 * Created: AriesHoo on 2018/2/9/009 9:32
 * E-Mail: AriesHoo@126.com
 * Function: 沉浸式状态栏控制帮助类
 * Description:
 */
public class StatusViewHelper {

    public final static int TAG_SET_STATUS_CONTROL = 0x10000012;
    private String TAG = getClass().getSimpleName();
    private SoftReference<Activity> mActivity;
    private boolean mLogEnable;
    private boolean mControlEnable;
    private boolean mTransEnable;
    private boolean mPlusStatusViewEnable;
    private int mStatusViewColor;
    private Drawable mStatusViewDrawable;
    private Drawable mStatusLayoutDrawable;
    private View mTopView;//设置activity最底部View用于增加导航栏的padding

    private View mContentView;//activity xml设置根布局
    private LinearLayout mLinearLayout;
    private LinearLayout mLayoutStatus;

    private StatusViewHelper(Activity activity) {
        mActivity = new SoftReference<>(activity);
        mContentView = ((ViewGroup) activity.getWindow().getDecorView()
                .findViewById(android.R.id.content)).getChildAt(0);
    }

    public static StatusViewHelper with(Activity activity) {
        if (activity == null) {
            throw new NullPointerException("null");
        }
        return new StatusViewHelper(activity);
    }

    /**
     * 是否打印log
     *
     * @param logEnable
     * @return
     */
    public StatusViewHelper setLogEnable(boolean logEnable) {
        mLogEnable = logEnable;
        return this;
    }

    /**
     * 设置是否控制虚拟导航栏
     *
     * @param controlEnable
     * @return
     */
    public StatusViewHelper setControlEnable(boolean controlEnable) {
        mControlEnable = controlEnable;
        if (!controlEnable) {
            setPlusStatusViewEnable(true)
                    .setStatusViewColor(Color.BLACK)
                    .setStatusLayoutColor(Color.BLACK);
        }
        return this;
    }

    /**
     * 设置是否全透明
     *
     * @param transEnable
     * @return
     */
    public StatusViewHelper setTransEnable(boolean transEnable) {
        this.mTransEnable = transEnable;
        if (mContentView != null && mContentView.getBackground() != null) {
            setStatusLayoutDrawable(mContentView.getBackground());
        } else {
            setStatusLayoutColor(Color.WHITE);
        }
        return setStatusViewColor(transEnable ? Color.TRANSPARENT : Color.argb(102, 0, 0, 0));
    }

    /**
     * 是否设置假的导航栏--用于沉浸遮挡
     *
     * @param plusStatusViewEnable
     * @return
     */
    public StatusViewHelper setPlusStatusViewEnable(boolean plusStatusViewEnable) {
        this.mPlusStatusViewEnable = plusStatusViewEnable;
        return this;
    }

    /**
     * 设置 StatusView背景颜色
     *
     * @param StatusViewColor ColorInt
     * @return
     */
    public StatusViewHelper setStatusViewColor(int StatusViewColor) {
        this.mStatusViewColor = StatusViewColor;
        return setStatusViewDrawable(new ColorDrawable(StatusViewColor));
    }

    /**
     * 设置假StatusView背景资源
     *
     * @param drawable
     * @return
     */
    public StatusViewHelper setStatusViewDrawable(Drawable drawable) {
        this.mStatusViewDrawable = drawable;
        return this;
    }

    /**
     * 设置假StatusView父ViewGroup背景颜色
     *
     * @param StatusLayoutColor ColorInt
     * @return
     */
    public StatusViewHelper setStatusLayoutColor(int StatusLayoutColor) {
        return setStatusLayoutDrawable(new ColorDrawable(StatusLayoutColor));
    }

    /**
     * 设置假StatusView父ViewGroup背景资源
     *
     * @param StatusLayoutDrawable
     * @return
     */
    public StatusViewHelper setStatusLayoutDrawable(Drawable StatusLayoutDrawable) {
        this.mStatusLayoutDrawable = StatusLayoutDrawable;
        return this;
    }

    /**
     * 设置最底部--虚拟状态栏上边的View
     *
     * @param view
     * @return
     */
    public StatusViewHelper setTopView(View view) {
        mTopView = view;
        return this;
    }

    /**
     * 开始设置StatusView相应效果
     */
    public void init() {
        Activity activity = mActivity.get();
        if (activity == null || activity.isFinishing()) {
            throw new NullPointerException("not exist");
        }
        setTransEnable(mTransEnable)
                .setControlEnable(mControlEnable);
        final Window window = activity.getWindow();
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && (mPlusStatusViewEnable || (!mPlusStatusViewEnable && mTransEnable))) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility()
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(!mPlusStatusViewEnable ? mStatusViewColor : Color.TRANSPARENT);
            }
        }
        addStatusBar(window);
        if (mLayoutStatus != null) {
            mLayoutStatus.setVisibility(mPlusStatusViewEnable ? View.VISIBLE : View.GONE);
        }

        if (mTopView != null) {
            if (!mPlusStatusViewEnable)
                mTopView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ViewGroup.LayoutParams params = mTopView.getLayoutParams();
                        if (params != null && params.height >= 0) {//默认
                            params.height = params.height + TitleBarView.getStatusBarHeight();
                        }
                        Object isSet = mTopView.getTag(TAG_SET_STATUS_CONTROL);
                        if (isSet == null) {
                            mTopView.setPadding(
                                    mTopView.getPaddingLeft(),
                                    mTopView.getPaddingTop() + TitleBarView.getStatusBarHeight(),
                                    mTopView.getPaddingRight(),
                                    mTopView.getPaddingBottom());
                            if (mLogEnable)
                                Log.i(TAG, "mTopView:" + mTopView + "设置成功");
                        }
                        mTopView.setTag(TAG_SET_STATUS_CONTROL, true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            mTopView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            mTopView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                    }
                });
        }
    }

    private void addStatusBar(Window window) {
        if (!isSupportStatusBarControl()) {
            return;
        }
        if (mLinearLayout == null) {
            mLinearLayout = FindViewUtil.getTargetView(window.getDecorView(), LinearLayout.class);
        }
        if (mLinearLayout != null && mPlusStatusViewEnable) {
            final LinearLayout linearLayout = mLinearLayout;
            Context mContext = window.getContext();
            //创建假的StatusView包裹ViewGroup用于设置背景与mContentView一致
            mLayoutStatus = (LinearLayout) linearLayout.findViewById(R.id.fake_status_layout);
            View viewStatus;
            if (mLayoutStatus == null) {
                mLayoutStatus = new LinearLayout(mContext);
                mLayoutStatus.setId(R.id.fake_status_layout);
                //创建假的StatusView
                viewStatus = new View(mContext);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, TitleBarView.getStatusBarHeight());
                viewStatus.setId(R.id.fake_status_view);
                mLayoutStatus.addView(viewStatus, params);
                linearLayout.addView(mLayoutStatus, 0,
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                viewStatus = mLayoutStatus.findViewById(R.id.fake_status_view);
            }
            if (mLayoutStatus != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mLayoutStatus.setBackground(mStatusLayoutDrawable);
                    viewStatus.setBackground(mStatusViewDrawable);
                } else {
                    mLayoutStatus.setBackgroundDrawable(mStatusLayoutDrawable);
                    viewStatus.setBackgroundDrawable(mStatusViewDrawable);
                }
            }
        }

    }

    protected boolean isSupportStatusBarControl() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * 获取window的DecorView的第一个LinearLayout用于设置假的StatusView
     *
     * @param rootView
     */
    private void getLinearLayout(View rootView) {
        if (rootView instanceof LinearLayout && mLinearLayout == null) {
            mLinearLayout = (LinearLayout) rootView;
            if (mLogEnable)
                Log.i(TAG, "ViewGroupName:" + rootView.getClass().getSimpleName() + ";children:" + mLinearLayout.getChildCount());
        } else if (rootView instanceof ViewGroup) {
            ViewGroup contentView = (ViewGroup) rootView;
            if (mLogEnable)
                Log.i(TAG, "ViewGroupName:" + rootView.getClass().getSimpleName() + ";children:" + contentView.getChildCount());
            int childCount = contentView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = contentView.getChildAt(i);
                getLinearLayout(childView);
            }
        } else {
            if (mLogEnable)
                Log.i(TAG, "ViewName:" + rootView.getClass().getSimpleName());
        }
    }

}

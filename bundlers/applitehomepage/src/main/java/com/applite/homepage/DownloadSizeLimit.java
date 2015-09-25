package com.applite.homepage;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applite.common.LogUtils;
import com.mit.impl.ImplConfig;
import com.osgi.extra.OSGIBaseFragment;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

/**
 * Created by android153 on 9/25/15.
 */
public class DownloadSizeLimit extends OSGIBaseFragment implements View.OnClickListener {
    private final String TAG = "DownloadSizeLimit";

    private LayoutInflater mInflater;
    private ViewGroup rootView;

    private LinearLayout limitOnoffTitle;
    private ActionBar actionBar;
    private ImageView download_limit_onoff;
    private TextView select_limit_detail;
    private int current;
    private String limitdetail;

    private WheelView mWheelView;
    private long size;

    private float scale = 1f;
    private int distance = 25;

    public DownloadSizeLimit() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        LogUtils.d(TAG, "onAttach ");
        super.onAttach(activity);
        mInflater = LayoutInflater.from(mActivity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = (ViewGroup) mInflater.inflate(R.layout.fragment_sizelimit, container, false);

        limitOnoffTitle = (LinearLayout)rootView.findViewById(R.id.download_limit_onoff_title);
        download_limit_onoff = (ImageView) rootView.findViewById(R.id.download_limit_onoff);
        download_limit_onoff.setOnClickListener(this);
        limitOnoffTitle.setVisibility(View.GONE);

        select_limit_detail = (TextView) rootView.findViewById(R.id.select_limit_detail);

        mWheelView = (WheelView) rootView.findViewById(R.id.max_size);
        String sizelist[] = mActivity.getResources().getStringArray(R.array.sizelist);
        mWheelView.setViewAdapter(new ArrayWheelAdapter<>(this.getActivity(), sizelist));
        mWheelView.setCyclic(false);
        mWheelView.setScaleX(scale);
        mWheelView.setScaleY(scale);
        mWheelView.setMinimumHeight(distance);

        current = getLimitSize();
        mWheelView.setCurrentItem(current);
        if (null != limitdetail) {
            select_limit_detail.setText(limitdetail);
        }

        mWheelView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheelView, int oldValue, int newValue) {
                setLimitSize(newValue);
            }
        });


//        mWheelView.addScrollingListener(new OnWheelScrollListener() {
//            @Override
//            public void onScrollingStarted(WheelView wheelView) {
//                //TODO
//            }
//
//            @Override
//            public void onScrollingFinished(WheelView wheelView) {
//                current = mWheelView.getCurrentItem();
//                LogUtils.d(TAG, "current = " + current);
//                setLimitSize(current);
//            }
//        });

        initActionBar();
        return rootView;
    }

    private void setLimitSize(int i) {
        switch (i) {
            case 0:     //没有限制
                size = Long.MAX_VALUE;
                limitdetail = getString(R.string.select_no_limit);
                break;
            case 1:     //20MB
                size = 20 * 1024 * 1000;
                limitdetail = getString(R.string.select_20_limit);
                break;
            case 2:     //10MB
                size = 10 * 1024 * 1000;
                limitdetail = getString(R.string.select_10_limit);
                break;
            case 3:     //5MB
                size = 5 * 1024 * 1000;
                limitdetail = getString(R.string.select_5_limit);
                break;
            case 4:     //2MB
                size = 2 * 1024 * 1000;
                limitdetail = getString(R.string.select_2_limit);
                break;
            case 5:     //1MB
                size = 1024 * 1000;
                limitdetail = getString(R.string.select_1_limit);
                break;
            case 6:     //512KB
                size = 512 * 1000;
                limitdetail = getString(R.string.select_less_1_limit);
                break;
            case 7:     //不允许下载
                size = 0;
                limitdetail = getString(R.string.select_all_limit);
                break;
            default:    //默认10M
                size = 10 * 1024 * 1000;
                limitdetail = getString(R.string.select_10_limit);
                break;
        }
        ImplConfig.setMaxOverSize(mActivity, size);
        if (null != select_limit_detail)
            select_limit_detail.setText(limitdetail);
    }

    private int getLimitSize() {
        long s;
        int i = 0;

        s = ImplConfig.getMaxOverSize(mActivity);

        if (s == Long.MAX_VALUE) {
            i = 0;
            limitdetail = getString(R.string.select_no_limit);
        } else if (s == 20 * 1024 * 1000) {
            i = 1;
            limitdetail = getString(R.string.select_20_limit);
        } else if (s == 10 * 1024 * 1000) {
            i = 2;
            limitdetail = getString(R.string.select_10_limit);
        } else if (s == 5 * 1024 * 1000) {
            i = 3;
            limitdetail = getString(R.string.select_5_limit);
        } else if (s == 2 * 1024 * 1000) {
            i = 4;
            limitdetail = getString(R.string.select_2_limit);
        } else if (s == 1024 * 1000) {
            i = 5;
            limitdetail = getString(R.string.select_1_limit);
        } else if (s == 512 * 1000) {
            i = 6;
            limitdetail = getString(R.string.select_less_1_limit);
        } else if (s == 0) {
            i = 7;
            limitdetail = getString(R.string.select_all_limit);
        }
        return i;
    }

    private void initActionBar() {
        if (null == actionBar) {
            actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mActivity.getResources().getString(R.string.download_size));
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.show();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (R.id.download_limit_onoff == v.getId()) {
            //TODO
        }
    }
}

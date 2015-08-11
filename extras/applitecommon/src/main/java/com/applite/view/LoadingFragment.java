package com.applite.view;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applite.common.LogUtils;
import com.applite.common.R;
import com.osgi.extra.OSGIBaseFragment;

public class LoadingFragment extends OSGIBaseFragment implements View.OnClickListener{
    private final String TAG = "LoadingFragment";

    private Activity mActivity;
    private ViewGroup rootView;
    private LayoutInflater mInflater;

    //网络接连断开显示的view
    RelativeLayout moffnetView;
    ImageView moffImg;
    Button mretryBtn;

    //加载过程中显示view
    RelativeLayout mloadingView;
    ImageView mloadingImg;

    //加载动画
    private AnimationDrawable LoadingAnimation;

    public LoadingFragment(Fragment mFragment, Bundle params) {
        super(mFragment, params);
    }

    public static OSGIBaseFragment newInstance(Fragment fg,Bundle params){
        return new LoadingFragment(fg,params);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LogUtils.d(TAG, "onAttach ");
        mActivity = activity;
        mInflater = LayoutInflater.from(mActivity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i(TAG, "ListFragment.onCreate() ");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate savedInstanceState : " + savedInstanceState);

        boolean networkState = detectNetworkState(getActivity());

        rootView = (ViewGroup)mInflater.inflate(R.layout.fragment_loading, container, false);

        moffnetView = (RelativeLayout) rootView.findViewById(R.id.offnet_view);
        moffImg = (ImageView) rootView.findViewById(R.id.off_img);
        mretryBtn = (Button) rootView.findViewById(R.id.retry_btn);

        mloadingView = (RelativeLayout) rootView.findViewById(R.id.loading_view);
        mloadingImg = (ImageView) rootView.findViewById(R.id.loading_img);
        mloadingImg.setBackgroundResource(R.drawable.load_ani);
        LoadingAnimation = (AnimationDrawable) mloadingImg.getBackground();

        return rootView;
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

    }



    public static boolean detectNetworkState(Activity act) {
        ConnectivityManager manager = (ConnectivityManager) act
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        if (networkinfo == null || !networkinfo.isAvailable()) {
            return false;
        }
        return true;
    }

}

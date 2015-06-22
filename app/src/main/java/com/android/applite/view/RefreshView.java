package com.android.applite.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applite.android.R;

public class RefreshView extends LinearLayout {
    private TextView mTextView;
    private ImageView mImageView;
//    private ProgressBar mProgressBar;
    private Animation mRotateAnim;

    public RefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public RefreshView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    
    public void setup(){
        mTextView = (TextView)findViewById(R.id.refresh_text);
        mImageView = (ImageView)findViewById(R.id.refresh_icon);
//        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
        mRotateAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        mRotateAnim.setInterpolator(new LinearInterpolator());
    }
    
    public void refreshPull(){
        mImageView.clearAnimation();
//        mTextView.setText(R.string.refresh_pull);
    }
    
    public void refreshPullOver(){
        mImageView.startAnimation(mRotateAnim);
//        mTextView.setText(R.string.refresh_pull_over);
    }
    
    public void refreshing(){
        mImageView.startAnimation(mRotateAnim);
//        mTextView.setText(R.string.refreshing);
    }
    
    public void refreshReset(){
        mImageView.setVisibility(View.VISIBLE);
//        mProgressBar.setVisibility(View.GONE);
        mImageView.clearAnimation();
    }
}

package com.applite.similarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applite.common.LogUtils;
import com.applite.common.R;

/**
 * Created by hxd on 15-9-1.
 */
public class SimilarView extends LinearLayout implements View.OnClickListener {
    public TextView mTitle;
    public GridView mGridView;
    public SimilarAdapter mAdapter;
    public TextView mChangeView;
    public TextView mDataNullView;

    private int mTitleId;
    private int mChangeId;
    private int mDateNullId;
    private int mGridId;
//    private List<SimilarBean> mSimilarData;

    public SimilarView(Context context) {
        super(context, null);
        initData(context, null, 0);
    }

    public SimilarView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initData(context, attrs, 0);
    }

    public SimilarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData(context, attrs, defStyle);
    }

    private void initData(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SimilarView, defStyle, 0);
        mTitleId = a.getResourceId(R.styleable.SimilarView_similarTitleView, R.id.similar_title);
        mChangeId = a.getResourceId(R.styleable.SimilarView_similarChangeView, R.id.similar_change);
        mDateNullId = a.getResourceId(R.styleable.SimilarView_similarDateNullView, R.id.similar_data_null);
        mGridId = a.getResourceId(R.styleable.SimilarView_similarGridView, R.id.similar_grid);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitle = (TextView) findViewById(mTitleId);
        mChangeView = (TextView) findViewById(mChangeId);
        mDataNullView = (TextView) findViewById(mDateNullId);
        mGridView = (GridView) findViewById(mGridId);
//        mTitle = (TextView) findViewById(R.id.similar_title);
//        mDataNullView = (TextView) findViewById(R.id.similar_data_null);
//        mChangeView = (TextView) findViewById(R.id.similar_change);
//        mGridView = (GridView) findViewById(R.id.similar_grid);
        mGridView.setFocusable(false);
        mChangeView.setOnClickListener(this);
    }

//    public void setData(List<SimilarBean> data,SimilarAdapter.SimilarAPKDetailListener listener){
//        mSimilarData = data;
//        if (null != mSimilarData && mSimilarData.size() > 0){
//            mGridView.setVisibility(View.VISIBLE);
//            if (null == mAdapter){
//                mAdapter = new SimilarAdapter(getContext());
//                mAdapter.setData(data,listener);
//                mGridView.setAdapter(mAdapter);
//            }else{
//                mAdapter.setData(data,listener);
//                mAdapter.notifyDataSetChanged();
//            }
//        }else{
//            mGridView.setVisibility(View.GONE);
//        }
//    }

    public void setAdapter(SimilarAdapter adapter) {
        if (null == adapter) {
            mGridView.setVisibility(View.GONE);
            return;
        }
        mAdapter = adapter;
        mGridView.setAdapter(adapter);
        mGridView.setVisibility(View.VISIBLE);
    }

    public SimilarAdapter getAdapter() {
        return mAdapter;
    }

    public void setTitle(String title) {
        this.mTitle.setText(title);
    }

    public void setTitle(int titleId) {
        this.mTitle.setText(getContext().getResources().getString(titleId));
    }

    /**
     * 设置换一换的显示和隐藏状态
     *
     * @param visibility
     */
    public void setChangeViewVisibility(int visibility) {
        mChangeView.setVisibility(visibility);
    }

    /**
     * 得到一行显示多少个图标
     *
     * @return
     */
    public int getNumColumns() {
        return mGridView.getNumColumns();
    }

    /**
     * 设置一行显示多少个图标
     *
     * @return
     */
    public void setNumColumns(int number) {
        mGridView.setNumColumns(number);
    }

    /**
     * 设置Item里面的安装按钮是否显示
     *
     * @param i
     */
    public void setItemButtonVisibility(int i) {
        mAdapter.setItemButtonVisibility(i);
    }

    /**
     * 设置Item的背景
     *
     * @param color
     */
    public void setItemBackgroundColor(int color) {
        mAdapter.setItemBackgroundColor(color);
    }

    /**
     * 设置Item的背景
     *
     * @param resId
     */
    public void setItemBackgroundResource(int resId) {
        mAdapter.setItemBackgroundResource(resId);
    }

    /**
     * 设置应用名的颜色
     *
     * @param color
     */
    public void setItemNameTextColor(int color) {
        mAdapter.setItemNameTextColor(color);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mChangeId) {
            if (null != mAdapter)
                if (null != mAdapter.mSimilarBeans && mAdapter.mSimilarBeans.size() > 0)
                    mAdapter.change();
        }
    }
}

package com.applite.similarview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import com.applite.common.R;

/**
 * Created by hxd on 15-9-1.
 */
public class SimilarView extends LinearLayout{
    private TextView mTitle;
    private GridView mGridView;
    private SimilarAdapter mAdapter;
    private List<SimilarBean> mSimilarData;

    public SimilarView(Context context) {
        super(context,null);
    }

    public SimilarView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public SimilarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitle = (TextView)findViewById(R.id.similar_title);
        mGridView = (GridView)findViewById(R.id.similar_grid);
    }

    public void setData(List<SimilarBean> data,SimilarAdapter.SimilarAPKDetailListener listener){
        mSimilarData = data;
        if (null != mSimilarData && mSimilarData.size() > 0){
            mGridView.setVisibility(View.VISIBLE);
            if (null == mAdapter){
                mAdapter = new SimilarAdapter(getContext());
                mAdapter.setData(data,listener);
                mGridView.setAdapter(mAdapter);
            }else{
                mAdapter.setData(data,listener);
                mAdapter.notifyDataSetChanged();
            }
        }else{
            mGridView.setVisibility(View.GONE);
        }
    }

    public void setTitle(String title) {
        this.mTitle.setText(title);
    }

    public void setTitle(int  titleId) {
        this.mTitle.setText(getContext().getResources().getString(titleId));
    }
}

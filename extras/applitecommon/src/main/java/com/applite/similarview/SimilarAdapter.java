package com.applite.similarview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.applite.common.BitmapHelper;
import com.applite.common.R;
import com.lidroid.xutils.BitmapUtils;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LSY on 15-8-12.
 */
public abstract class SimilarAdapter<T> extends BaseAdapter {
    private final Context mContext;
    public List<T> mSimilarBeans;
    public List<T> mShowSimilarBeans = new ArrayList<T>();
    private final BitmapUtils mBitmapUtil;
    public SimilarAPKDetailListener mSimilarAPKDetailListener;

    private int mPageNumber;//数据的页数
    private int mEndPageIconNumber;//最后一页个数
    private int mChangeNumber = 0;//点击换一换的次数
    private int mNumColumns;//一页显示的个数

    private boolean isItemButtonShow = true;
    private int mItemBackgroundColor = -1;
    private int mItemBackgroundResource = -1;
    private int mItemNameTextColor = -1;

    public interface SimilarAPKDetailListener<T> {
        void onClickIcon(Object... params);

        void onClickName(Object... params);

        void onClickButton(Object... params);

        void dataLess(int dataNumber);
    }

    public SimilarAdapter(Context context) {
        mContext = context;
        mBitmapUtil = BitmapHelper.getBitmapUtils(mContext.getApplicationContext());
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public int getCount() {
        return mShowSimilarBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return mShowSimilarBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.item_similar, parent, false);
            if (mItemBackgroundColor != -1)
                convertView.setBackgroundColor(mItemBackgroundColor);
            if (mItemBackgroundResource != -1)
                convertView.setBackgroundResource(mItemBackgroundResource);
            viewholder = newViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }
        final T data = mShowSimilarBeans.get(position);
        if (data instanceof SimilarBean) {
            SimilarBean bean = (SimilarBean) data;
            viewholder.initView(data);
            mBitmapUtil.display(viewholder.mIcon, bean.getIconUrl());
            viewholder.mName.setText(bean.getName());
        }
        return convertView;
    }

    public abstract ViewHolder newViewHolder(View convertView);

    /**
     * 设置数据
     *
     * @param data
     * @param listener
     * @param NumColumns 列数
     */
    public void setData(List<T> data, SimilarAPKDetailListener<T> listener, int NumColumns) {
        mSimilarBeans = data;
        mSimilarAPKDetailListener = listener;
        mNumColumns = NumColumns;
        mShowSimilarBeans.clear();
        mSimilarAPKDetailListener.dataLess(mSimilarBeans.size());
        if (mSimilarBeans.size() > mNumColumns) {//判断数据的个数    是否大于一行个个数
            for (int i = 0; i < NumColumns; i++) {
                mShowSimilarBeans.add(mSimilarBeans.get(i));
            }
        } else {
            for (int i = 0; i < mSimilarBeans.size(); i++) {
                mShowSimilarBeans.add(mSimilarBeans.get(i));
            }
        }
        mEndPageIconNumber = mSimilarBeans.size() % NumColumns;
        if (mEndPageIconNumber == 0) {
            mPageNumber = mSimilarBeans.size() / NumColumns;
        } else {
            mPageNumber = mSimilarBeans.size() / NumColumns + 1;
        }
    }

    /**
     * 换一换
     */
    public void change() {
        mChangeNumber = mChangeNumber + 1;
        mShowSimilarBeans.clear();

        if (mSimilarBeans.size() > mNumColumns) {//总数据大于一页显示的个数
            if (mChangeNumber == mPageNumber - 1 && mEndPageIconNumber != 0) {//是否最后一页且最后一页不满一页个个数
                for (int i = mSimilarBeans.size() - mNumColumns; i < mSimilarBeans.size(); i++) {
                    mShowSimilarBeans.add(mSimilarBeans.get(i));
                }
            } else {
                for (int i = 0 + mNumColumns * mChangeNumber; i < mNumColumns + mNumColumns * mChangeNumber; i++) {
                    mShowSimilarBeans.add(mSimilarBeans.get(i));
                }
            }
        } else {
            for (int i = 0; i < mSimilarBeans.size(); i++) {
                mShowSimilarBeans.add(mSimilarBeans.get(i));
            }
            mSimilarAPKDetailListener.dataLess(mSimilarBeans.size());
        }
        if (mChangeNumber == mPageNumber - 1)
            mChangeNumber = -1;
        notifyDataSetChanged();
    }

    /**
     * 设置Item里面的安装按钮是否显示
     *
     * @param i
     */
    public void setItemButtonVisibility(int i) {
        if (i == View.GONE) {
            isItemButtonShow = false;
        } else if (i == View.VISIBLE) {
            isItemButtonShow = true;
        }
    }

    /**
     * 设置Item的背景
     *
     * @param color
     */
    public void setItemBackgroundColor(int color) {
        mItemBackgroundColor = color;
    }

    /**
     * 设置Item的背景
     *
     * @param resId
     */
    public void setItemBackgroundResource(int resId) {
        mItemBackgroundResource = resId;
    }

    /**
     * 设置应用名的颜色
     *
     * @param color
     */
    public void setItemNameTextColor(int color) {
        mItemNameTextColor = color;
    }

    public class ViewHolder implements View.OnClickListener {
        public TextView mName;
        public ImageView mIcon;
        public TextView mTv;
        public T bean;

        public ViewHolder(View view) {
            this.mIcon = (ImageView) view.findViewById(R.id.item_similar_img);
            this.mName = (TextView) view.findViewById(R.id.item_similar_name);
            this.mTv = (TextView) view.findViewById(R.id.item_similar_install_tv);
            mIcon.setOnClickListener(this);
            mName.setOnClickListener(this);
            mTv.setOnClickListener(this);
            if (isItemButtonShow) {
                mTv.setVisibility(View.VISIBLE);
            } else {
                mTv.setVisibility(View.GONE);
            }
            if (mItemNameTextColor != -1)
                mName.setTextColor(mItemNameTextColor);
        }

        public void initView(T data) {
            bean = data;
            mTv.setTag(this);
            refresh();
        }

        public void refresh() {
        }

        @Override
        public void onClick(View v) {
//            if (v.getId() == R.id.item_similar_img) {
//                mSimilarAPKDetailListener.onClickIcon(bean);
//            } else if (v.getId() == R.id.item_similar_name) {
//                mSimilarAPKDetailListener.onClickName(bean);
//            } else if (v.getId() == R.id.item_similar_install_tv) {
//                mSimilarAPKDetailListener.onClickButton(mTv);
//            }
        }
    }
}

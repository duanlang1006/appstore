package com.applite.similarview;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.applite.common.BitmapHelper;
import com.applite.common.R;
import com.lidroid.xutils.BitmapUtils;
import java.util.List;

/**
 * Created by LSY on 15-8-12.
 */
public abstract class SimilarAdapter extends BaseAdapter {
    private final Context mContext;
    private List<SimilarBean> mSimilarBeans;
    private final BitmapUtils mBitmapUtil;
    private SimilarAPKDetailListener mSimilarAPKDetailListener;

    public interface SimilarAPKDetailListener {
        void refreshDetail(SimilarBean bean);
    }

    public SimilarAdapter(Context context) {
        mContext = context;
        mBitmapUtil = BitmapHelper.getBitmapUtils(mContext.getApplicationContext());
    }

    public Context getContext(){
        return mContext;
    }

    @Override
    public int getCount() {
        return mSimilarBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return mSimilarBeans.get(position);
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
            viewholder = newViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }
        final SimilarBean data = mSimilarBeans.get(position);
        viewholder.initView(data);
        mBitmapUtil.display(viewholder.mImg, data.getIconUrl());
        viewholder.mName.setText(data.getName());
        return convertView;
    }

    public void setData(List<SimilarBean> data, SimilarAPKDetailListener listener){
        mSimilarBeans = data;
        mSimilarAPKDetailListener = listener;
    }

    public abstract ViewHolder newViewHolder(View convertView);

    public class ViewHolder {
        public TextView mName;
        public ImageView mImg;
        public TextView mTv;
        public SimilarBean bean;

        public ViewHolder(View view) {
            this.mImg = (ImageView) view.findViewById(R.id.item_similar_img);
            this.mName = (TextView) view.findViewById(R.id.item_similar_name);
            this.mTv = (TextView) view.findViewById(R.id.item_similar_install_tv);
            mImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSimilarAPKDetailListener.refreshDetail(bean);
                }
            });
        }

        public void initView(SimilarBean data) {
            bean = data;
            mTv.setTag(this);
            refresh();
        }

        public void refresh(){};
    }
}

package com.mit.applite.search.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mit.applite.search.R;
import com.mit.applite.search.bean.HotWordBean;
import com.mit.applite.search.main.BundleContextFactory;
import com.mit.applite.search.utils.SearchUtils;

import net.tsz.afinal.FinalBitmap;

import java.util.List;

/**
 * Created by LSY on 15-5-28.
 */
public class HotWordAdapter extends BaseAdapter {

    private final FinalBitmap mFinalBitmap;
    private Context context;
    private List<HotWordBean> mHotWordBeans;
    private LayoutInflater mInflater;

    public HotWordAdapter(Context context, List<HotWordBean> mHotWordBeans) {
        this.mHotWordBeans = mHotWordBeans;
        mFinalBitmap = FinalBitmap.create(context);
        try {
            Context mContext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            this.context = mContext;
            mInflater = LayoutInflater.from(mContext);
            mInflater = mInflater.cloneInContext(mContext);
        } catch (Exception e) {
            e.printStackTrace();
            mInflater = LayoutInflater.from(context);
            this.context = context;
        }
    }

    @Override
    public int getCount() {
        return mHotWordBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return mHotWordBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;
        /* 将convertView封装在ViewHodler中，减少系统内存占用 */
        if (convertView == null) {
            /* convertView为空则初始化 */
            convertView = mInflater.inflate(R.layout.item_hot_word_iv, parent, false);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            // 不为空则直接使用已有的封装类
            viewholder = (ViewHolder) convertView.getTag();
        }
        final HotWordBean data = mHotWordBeans.get(position);
        if (Integer.parseInt(data.getmType()) == 1) {
            mFinalBitmap.display(viewholder.mImg, data.getmImgUrl());
            viewholder.mImg.setVisibility(View.VISIBLE);
        }
        viewholder.mTv.setText(data.getmName());
        viewholder.mLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchUtils.toDetailFragment(data.getmPackageName(), data.getmName(), data.getmImgUrl());
            }
        });
        return convertView;
    }

    class ViewHolder {
        private LinearLayout mLl;
        private ImageView mImg;
        private TextView mTv;

        public ViewHolder(View view) {
            this.mLl = (LinearLayout) view.findViewById(R.id.item_hot_word_iv_ll);
            this.mImg = (ImageView) view.findViewById(R.id.item_hot_word_iv_iv);
            this.mTv = (TextView) view.findViewById(R.id.item_hot_word_iv_tv);
        }

    }

}

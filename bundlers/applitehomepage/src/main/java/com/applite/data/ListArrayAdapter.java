package com.applite.data;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.applite.bean.HomePageTypeBean;
import com.applite.bean.HomePageBean;
import com.applite.common.LogUtils;
import com.applite.homepage.BundleContextFactory;
import com.mit.impl.ImplStatusTag;
import com.applite.homepage.R;
import net.tsz.afinal.FinalBitmap;


import java.util.List;

//import android.widget.RelativeLayout.LayoutParams;

/**
 * Created by yuzhimin on 6/17/15.
 */
public class ListArrayAdapter extends ArrayAdapter<HomePageBean> implements View.OnClickListener {
    private static final String TAG = "homepage_ListArrayAdapter";
    private LayoutInflater mInflater = null;
    private Context mContext;
    private int layoutResourceId;
    private List<HomePageBean> mData = null;

    private FinalBitmap mFinalBitmap;
    private int mTable = 0;
    private List<HomePageTypeBean> mDataType = null;
    private ListAdapterListener mListener = null;

    public ListArrayAdapter(Context context,
                            int resource,
                            List<HomePageBean> data,
                            List<HomePageTypeBean> dataType,
                            int mTab,
                            ListAdapterListener listener) {
        super(context, resource, data);
        this.mContext = context;
        this.layoutResourceId = resource;
        this.mData = data;
        this.mTable = mTab;
        this.mDataType = dataType;
        this.mListener = listener;
        mFinalBitmap = FinalBitmap.create(context);
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {
            Context mContext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            if (null != mContext) {
                mInflater = LayoutInflater.from(mContext);
                mInflater = mInflater.cloneInContext(mContext);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        if(null != mData) {
            return this.mData.size();
        }else if (null != mDataType){
            return this.mDataType.size();
        }
        return 0;
    }

    @Override
    public HomePageBean getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position == 0)
            return 0;
        else
            return position-1;
    }
    @Override
    public int getItemViewType(int position) {
        return position > 0 ? 0 : 1;

    }
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        float mStaring = 0.0f;
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResourceId, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        HomePageBean item = null;
        HomePageTypeBean itemType = null;
        try {
            if (null != mData) {
                LogUtils.i(TAG, "ListAdapter.getView position : " + position);
                item = mData.get(position);
                LogUtils.i(TAG, "ListAdapter.getView item : " + item);
                String localUri = item.getLocalUri();
                viewHolder.statusTag = ImplStatusTag.generateTag(mContext,
                        item.getPackagename(),
                        item.getPackagename(),
                        item.getName(),
                        item.getImgurl(),
                        item.getStatus(),
                        item.getReason(),
                        item.getCurrentBytes(),
                        item.getTotalBytes(),
                        (null == localUri) ? null : Uri.parse(localUri),
                        item.getMediaType());
            } else if (null != mDataType) {
                itemType = mDataType.get(position);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != item || null != itemType) {
            try {
                mStaring = Float.parseFloat(item.getRating().toString());
            } catch (Exception e) {
                mStaring = 0.0f;
            }
            switch (mTable) {
                case 0:
                case 1:
                    viewHolder.mProgressButton.setTag(viewHolder.statusTag);
                    viewHolder.mProgressButton.setText(viewHolder.statusTag.getActionText());
                    viewHolder.mProgressButton.setOnClickListener(this);
                    viewHolder.mAppName.setText(item.getName().toString());
                    viewHolder.mAppSize.setText(item.getCategorySub().toString());
                    viewHolder.mRatingBar.setRating(mStaring / 2.0f);
                    mFinalBitmap.display(viewHolder.mAppIcon, item.getImgurl());
                    break;
                case 2:
                    //LogUtils.i(TAG, "ListAdapter.getView item : " + item);
                    LogUtils.i(TAG, "ListAdapter.getView getM_IconUrl : " + itemType.getM_IconUrl());
                    try {
                        if ((null != itemType.getM_IconUrl())) {
                            mFinalBitmap.display(viewHolder.mAppIcon, itemType.getM_IconUrl());
                        } else {
                            viewHolder.mAppIcon.setImageResource(R.drawable.buffer);
                        }
                        convertView.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                        viewHolder.mImageView.setImageResource(R.drawable.back);
                        viewHolder.mAppName.setText(itemType.getM_Name().toString());
                        convertView.findViewById(R.id.textView2).setVisibility(View.GONE);
                        convertView.findViewById(R.id.ratingbar_Indicator).setVisibility(View.GONE);
                        convertView.findViewById(R.id.list_item_progress_button).setVisibility(View.GONE);
                    } catch (Exception e) {
                        viewHolder.mAppIcon.setImageResource(R.drawable.buffer);
                        convertView.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                        viewHolder.mImageView.setImageResource(R.drawable.back);
                        viewHolder.mAppName.setText(itemType.getM_Name().toString());
                        convertView.findViewById(R.id.textView2).setVisibility(View.GONE);
                        convertView.findViewById(R.id.ratingbar_Indicator).setVisibility(View.GONE);
                        convertView.findViewById(R.id.list_item_progress_button).setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                    break;
            }
        }else {
//            LogUtils.i(TAG, "ListAdapter.getView yuzm item : " + item);
        }
        return convertView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.list_item_progress_button){
            Object obj = v.getTag();
            if (obj instanceof ImplStatusTag){
                ImplStatusTag tag = (ImplStatusTag)obj;
                if (null != mListener){
                    mListener.onDownloadButtonClicked(tag);
                }
            }
        }
    }

    public void setDownloadListener(ListAdapterListener l){
        mListener = l;
    }

    public int setData(List<HomePageBean> data,List<HomePageTypeBean> dataType, int mType) {
        LogUtils.i(TAG, "ListAdapter.setData yuzm before data : " + data +
                "data.size() : " + data.size());
        //LogUtils.i(TAG, "ListAdapter.setData yuzm before mData.size() : " + mData.size());
        LogUtils.i(TAG, "getView() yuzm Thread.currentThread().getId() : " +
                Thread.currentThread().getId());
        switch (mType) {
            case 0 :
            case 1 : for (int i = 0; null != data && i < data.size(); i++) {
                        this.mData.add(data.get(i));
                    }
                return this.mData.size();
            case 2 :  for (int i = 0; null != data && i < data.size(); i++) {
                        this.mDataType.add(dataType.get(i));
                }
                return this.mDataType.size();
            default:
                return 0;
        }

        //LogUtils.i(TAG, "ListAdapter.setData yuzm after mData.size() : " + mData.size());
    }

    class ViewHolder {
        ImageView mAppIcon;
        TextView mAppName;
        TextView mAppSize;
        RatingBar mRatingBar;
        ImageView mImageView;
        Button mProgressButton;
        ImplStatusTag statusTag;

        ViewHolder(View mView){
            this.mAppIcon = (ImageView) mView.findViewById(R.id.imageViewName);
            this.mAppName = (TextView) mView.findViewById(R.id.textView);
            this.mAppSize = (TextView) mView.findViewById(R.id.textView2);
            this.mRatingBar = (RatingBar) mView.findViewById(R.id.ratingbar_Indicator);
            this.mImageView = (ImageView) mView.findViewById(R.id.imageView);
            this.mProgressButton = (Button) mView.findViewById(R.id.list_item_progress_button);
        }

        public ImplStatusTag getStatusTag() {
            return statusTag;
        }

        public void setStatusTag(ImplStatusTag statusTag) {
            this.statusTag = statusTag;
        }
    }

    public interface ListAdapterListener {
        public void onDownloadButtonClicked(ImplStatusTag tag);
    }
}

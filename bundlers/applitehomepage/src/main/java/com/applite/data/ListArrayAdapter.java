package com.applite.data;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.applite.bean.HomePageApkData;
import com.applite.bean.HomePageDataBean;

import com.applite.bean.SubjectData;
import com.applite.bean.HomePageBean;
import com.applite.common.LogUtils;
import com.applite.homepage.BundleContextFactory;
import com.mit.impl.ImplStatusTag;
import com.applite.homepage.R;
import net.tsz.afinal.FinalBitmap;

import java.lang.reflect.Field;

import java.util.List;

//import android.widget.RelativeLayout.LayoutParams;

/**
 * Created by yuzhimin on 6/17/15.
 */
public class ListArrayAdapter extends BaseAdapter implements View.OnClickListener {
    private static final String TAG = "homepage_ListArrayAdapter";
    private LayoutInflater mInflater = null;
    private Context mContext = null;
    private SubjectData mData = null;
    private FinalBitmap mFinalBitmap;

    private ListAdapterListener mListener = null;
    int layoutResourceId = 0;
    public ListArrayAdapter(Context context, SubjectData data,ListAdapterListener listener) {

        super();
        this.mContext = context;
        this.mData = data;
        this.mListener = listener;
        mFinalBitmap = FinalBitmap.create(context);

        LogUtils.i(TAG, "-----------------");
        if(null != mData) {
            for (int i = 0; i < mData.getHomePageApkData().size(); i++) {
                LogUtils.i(TAG, "ListAdapter.ListAdapter() yuzm mData.get(" + i +
                        ").getName() : " + mData.getHomePageApkData().get(i).getName()
                        + "/n getIconUrl : " + mData.getHomePageApkData().get(i).getIconUrl());
            }
        }
        LogUtils.i(TAG, "-----------------");
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {

            mContext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            if (null != mContext) {
                mInflater = LayoutInflater.from(mContext);
                mInflater = mInflater.cloneInContext(mContext);

                Field field=R.layout.class.getField(mData.getS_DataType());
                layoutResourceId= field.getInt(new R.layout());

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        LogUtils.i(TAG,"ListAdapter.ListAdapter()");

    }

    @Override
    public int getCount() {
        if(null != mData) {
            LogUtils.i(TAG, "ListAdapter.ListAdapter() this.data.size() : " + this.mData.getHomePageApkData().size());
            return this.mData.getHomePageApkData().size();
        }
        return 0;
    }

    @Override
    public HomePageBean getItem(int position) {
        LogUtils.i(TAG,"ListAdapter.getItem() position : " + position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        LogUtils.i(TAG,"ListAdapter.getItemId() position : " + position);
        if (position == 0)
            return 0;
        else
            return position-1;
    }
    @Override
    public int getItemViewType(int position) {
        LogUtils.i(TAG,"ListAdapter.getItemViewType() position : " + position);
        return position > 0 ? 0 : 1;

    }
    @Override
    public int getViewTypeCount() {
        LogUtils.i(TAG,"ListAdapter.getItemViewType() ");
        return 2;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        float mStaring = 0.0f;
        ViewHolder viewHolder = null;


        if (convertView == null) {
            convertView = mInflater.inflate(layoutResourceId, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        List<HomePageApkData> item = null;
        try {
            if (null != mData) {
                LogUtils.i(TAG, "ListAdapter.getView position : " + position);
                item = mData.getHomePageApkData();
                LogUtils.i(TAG, "ListAdapter.getView item : " + item);
                String localUri = item.get(position).getLocalUri();
                viewHolder.statusTag = ImplStatusTag.generateTag(mContext,
                        item.get(position).getPackageName(),
                        item.get(position).getPackageName(),
                        item.get(position).getName(),
                        item.get(position).getIconUrl(),
                        item.get(position).getStatus(),
                        item.get(position).getReason(),
                        item.get(position).getCurrentBytes(),
                        item.get(position).getTotalBytes(),
                        (null == localUri) ? null : Uri.parse(localUri),
                        item.get(position).getMediaType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != item) {
            try {
                mStaring = Float.parseFloat(item.get(position).getRating().toString());
            } catch (Exception e) {
                mStaring = 0.0f;
            }

            //viewHolder.mProgressButton.setTag(viewHolder.statusTag);
            //viewHolder.mProgressButton.setText(viewHolder.statusTag.getActionText());
            if (null != viewHolder.mProgressButton) {
                viewHolder.mProgressButton.setOnClickListener(this);
            }
            String mString =null;
            mString = item.get(position).getName().toString();
            if(null != mString) {
                viewHolder.mAppName.setText(mString);
            }
            if(null !=item.get(position).getCategorySub()) {
                mString = item.get(position).getCategorySub().toString();
                if (null != mString) {
                    viewHolder.mAppSize.setText(mString);
                }
                mString = null;
            }
            if(null !=item.get(position).getRating()) {
                mString = item.get(position).getRating().toString();
                if (null != mString) {
                    viewHolder.mRatingBar.setRating(mStaring / 2.0f);
                }
                mString = null;
            }
            if(null !=item.get(position).getIconUrl()) {
                mString = item.get(position).getIconUrl().toString();
                if (null != mString) {
                    mFinalBitmap.display(viewHolder.mAppIcon, mString);
                }
                mString = null;
            }
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

    public int setData(HomePageDataBean data) {
        LogUtils.i(TAG, "ListAdapter.setData before data : " + data +
                "data.size() : " + data.getSubjectData().size());
        //HomePageUtils.i(TAG, "ListAdapter.setData before mData.size() : " + mData.size());
        /*LogUtils.i(TAG, "getView() Thread.currentThread().getId() : " +
                Thread.currentThread().getId());
        for (int i = 0; null != data && i < data.size(); i++) {
           this.mData.getSubjectData().get(i).getHomePageApkData().add(data.get(i));
        }*/
        return this.mData.getHomePageApkData().size();

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

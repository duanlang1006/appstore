package com.applite.data;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.applite.bean.HomePageApkData;
import com.applite.bean.HomePageDataBean;
import com.applite.bean.HomePageTypeBean;
import com.applite.bean.SubjectData;
import com.applite.common.Constant;
import com.applite.bean.HomePageBean;
import com.applite.common.LogUtils;
import com.applite.homepage.BundleContextFactory;
import com.mit.impl.ImplStatusTag;
import com.applite.homepage.R;
import net.tsz.afinal.FinalBitmap;

import java.util.ArrayList;
import java.util.List;

//import android.widget.RelativeLayout.LayoutParams;

/**
 * Created by yuzhimin on 6/17/15.
 */
public class ListArrayAdapter extends BaseAdapter implements View.OnClickListener {
    private static final String TAG = "homepage_ListArrayAdapter";
    private LayoutInflater mInflater = null;
    private Context mContext;
    private int layoutResourceId;
    private SubjectData mData = null;
    private FinalBitmap mFinalBitmap;

    private ListAdapterListener mListener = null;

    public ListArrayAdapter(Context context, int resource, SubjectData data,ListAdapterListener listener) {

        super();
        this.mContext = context;
        this.layoutResourceId = resource;
        this.mData = data;
        this.mListener = listener;
        mFinalBitmap = FinalBitmap.create(context);

        LogUtils.i(TAG, "-----------------");
        /*if(null != mData) {
            for (int i = 0; i < mData.size(); i++) {
                HomePageUtils.i(TAG, "ListAdapter.ListAdapter() yuzm mData.get(" + i +
                        ").getName() : " + mData.get(i).getName());
            }
        }*/
        LogUtils.i(TAG, "-----------------");
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
        LogUtils.i(TAG,"ListAdapter.ListAdapter()");

    }

    @Override
    public int getCount() {
        List<HomePageApkData> apkList = mData.getHomePageApkData();
        if(null != apkList) {
            LogUtils.i(TAG, "ListAdapter.ListAdapter() yuzm this.data.size() : " + apkList.size());
            return apkList.size();
        }
        return 0;
    }

    @Override
    public HomePageBean getItem(int position) {
        LogUtils.i(TAG,"ListAdapter.getItem() yuzm position : " + position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        LogUtils.i(TAG,"ListAdapter.getItemId() yuzm position : " + position);
        if (position == 0)
            return 0;
        else
            return position-1;
    }
    @Override
    public int getItemViewType(int position) {
        LogUtils.i(TAG,"ListAdapter.getItemViewType() yuzm position : " + position);
        return position > 0 ? 0 : 1;

    }
    @Override
    public int getViewTypeCount() {
        LogUtils.i(TAG,"ListAdapter.getItemViewType() yuzm ");
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

        if (null != mData) {
            viewHolder.data = mData.getHomePageApkData().get(position);
            String localUri = viewHolder.data.getLocalUri();
            viewHolder.statusTag = ImplStatusTag.generateTag(mContext,
                    viewHolder.data.getPackageName(),
                    viewHolder.data.getPackageName(),
                    viewHolder.data.getName(),
                    viewHolder.data.getIconUrl(),
                    viewHolder.data.getStatus(),
                    viewHolder.data.getReason(),
                    viewHolder.data.getCurrentBytes(),
                    viewHolder.data.getTotalBytes(),
                    (null == localUri) ? null : Uri.parse(localUri),
                    viewHolder.data.getMediaType());

        }
        if (null != viewHolder.data) {
            try {
                mStaring = Float.parseFloat(viewHolder.data.getRating().toString());
            } catch (Exception e) {
                mStaring = 0.0f;
            }
            try {
                //viewHolder.mProgressButton.setTag(viewHolder.statusTag);
                //viewHolder.mProgressButton.setText(viewHolder.statusTag.getActionText());
                viewHolder.mProgressButton.setOnClickListener(this);
                viewHolder.mAppName.setText(viewHolder.data.getName().toString());
                viewHolder.mAppSize.setText(viewHolder.data.getCategorySub().toString());
                viewHolder.mRatingBar.setRating(mStaring / 2.0f);
                mFinalBitmap.display(viewHolder.mAppIcon, viewHolder.data.getIconUrl());
            }catch (Exception e){
                e.printStackTrace();
            }
              //LogUtils.i(TAG, "ListAdapter.getView item : " + item);
              /*LogUtils.i(TAG, "ListAdapter.getView getM_IconUrl : " + itemType.getM_IconUrl());
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
              */

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

    public int setData(HomePageDataBean data) {
        LogUtils.i(TAG, "ListAdapter.setData yuzm before data : " + data +
                "data.size() : " + data.getSubjectData().size());
        //HomePageUtils.i(TAG, "ListAdapter.setData before mData.size() : " + mData.size());
        /*HomePageUtils.i(TAG, "getView() yuzm Thread.currentThread().getId() : " +
                Thread.currentThread().getId());
        for (int i = 0; null != data && i < data.size(); i++) {
           this.mData.getSubjectData().get(i).getHomePageApkData().add(data.get(i));
        }*/
        return this.mData.getHomePageApkData().size();

    }

    public class ViewHolder {
        ImageView mAppIcon;
        TextView mAppName;
        TextView mAppSize;
        RatingBar mRatingBar;
        ImageView mImageView;
        Button mProgressButton;
        ImplStatusTag statusTag;
        HomePageApkData data;

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

        public HomePageApkData getData() {
            return data;
        }
    }

    public interface ListAdapterListener {
        public void onDownloadButtonClicked(ImplStatusTag tag);
    }
}

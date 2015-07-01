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
    private HomePageDataBean mData = null;
    private FinalBitmap mFinalBitmap;




    private ListAdapterListener mListener = null;

    public ListArrayAdapter(Context context, int resource, HomePageDataBean data,ListAdapterListener listener) {

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
        if(null != mData) {
            LogUtils.i(TAG, "ListAdapter.ListAdapter() yuzm this.data.size() : " + this.mData.getSubjectData().size());
            return this.mData.getSubjectData().size();
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

        SubjectData item = null;
        HomePageTypeBean itemType = null;
        try {
            if (null != mData) {
                LogUtils.i(TAG, "ListAdapter.getView position : " + position);
                item = mData.getSubjectData().get(position);
                LogUtils.i(TAG, "ListAdapter.getView item : " + item);
                String localUri = item.getHomePageApkData().get(position).getLocalUri();
                viewHolder.statusTag = ImplStatusTag.generateTag(mContext,
                        item.getHomePageApkData().get(position).getPackageName(),
                        item.getHomePageApkData().get(position).getPackageName(),
                        item.getHomePageApkData().get(position).getName(),
                        item.getHomePageApkData().get(position).getIconUrl(),
                        item.getHomePageApkData().get(position).getStatus(),
                        item.getHomePageApkData().get(position).getReason(),
                        item.getHomePageApkData().get(position).getCurrentBytes(),
                        item.getHomePageApkData().get(position).getTotalBytes(),
                        (null == localUri) ? null : Uri.parse(localUri),
                        item.getHomePageApkData().get(position).getMediaType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != item || null != itemType) {
            try {
                mStaring = Float.parseFloat(item.getHomePageApkData().get(position).getRating().toString());
            } catch (Exception e) {
                mStaring = 0.0f;
            }

             viewHolder.mProgressButton.setTag(viewHolder.statusTag);
             viewHolder.mProgressButton.setText(viewHolder.statusTag.getActionText());
             viewHolder.mProgressButton.setOnClickListener(this);
             viewHolder.mAppName.setText(item.getHomePageApkData().get(position).getName().toString());
             viewHolder.mAppSize.setText(item.getHomePageApkData().get(position).getCategorySub().toString());
             viewHolder.mRatingBar.setRating(mStaring / 2.0f);
             mFinalBitmap.display(viewHolder.mAppIcon, item.getHomePageApkData().get(position).getIconUrl());

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
        return this.mData.getSubjectData().size();

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

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
import com.applite.bean.SubjectData;
import com.applite.common.AppliteUtils;
import com.applite.common.LogUtils;
import com.applite.homepage.BundleContextFactory;
import com.mit.impl.ImplStatusTag;
import com.applite.homepage.R;
import net.tsz.afinal.FinalBitmap;
import java.lang.reflect.Field;
import java.util.List;

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
        this.mContext = context;
        this.mData = data;
        this.mListener = listener;
        mFinalBitmap = FinalBitmap.create(context);

        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {
            Context mcontext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            mInflater = LayoutInflater.from(mcontext);
            mInflater = mInflater.cloneInContext(mcontext);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            Field field = R.layout.class.getField(mData.getS_datatype());
            layoutResourceId = field.getInt(new R.layout());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        List<HomePageApkData> apkList = mData.getData();
        if(null != apkList) {
            return apkList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResourceId, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        LogUtils.d(TAG, "getView() mData : " + mData);
        if (null != mData && null != mData.getData()) {
            HomePageApkData itemData = mData.getData().get(position);
            LogUtils.d(TAG, "getView() itemData : " + itemData);
            viewHolder.setItemData(itemData);
            String localUri = itemData.getLocalUri();
            viewHolder.setStatusTag(ImplStatusTag.generateTag(mContext,
                    itemData.getPackageName(),
                    itemData.getPackageName(),
                    itemData.getName(),
                    itemData.getIconUrl(),
                    itemData.getStatus(),
                    itemData.getReason(),
                    itemData.getCurrentBytes(),
                    itemData.getTotalBytes(),
                    (null == localUri) ? null : Uri.parse(localUri),
                    itemData.getMediaType()));
            viewHolder.setLayoutStr(mData.getS_datatype());
            float star = 0.0f;
            try {
                star = Float.parseFloat(itemData.getRating());
            } catch (Exception e) {
            }
            viewHolder.setmRatingBar(star/2.0f);
            viewHolder.setmProgressButton(viewHolder.statusTag);
            viewHolder.setmAppIcon(itemData.getIconUrl());
            viewHolder.setCategorySub(itemData.getCategorysub());
            viewHolder.setAppSize(itemData.getApkSize());
            viewHolder.setmAppName(itemData.getName());
            viewHolder.setImageListArrow();
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

    public class ViewHolder {
        private ImageView mAppIcon;
        private TextView mAppName;
        private TextView mCategorySub;
        private TextView mAppSize;
        private RatingBar mRatingBar;
        private Button mProgressButton;

        private ImplStatusTag statusTag;
        private HomePageApkData itemData;
        private String layoutStr;
        private ImageView mCategoryListArrow;
        ViewHolder(View mView){

            this.mAppIcon = (ImageView) mView.findViewById(R.id.imageViewName);
            this.mAppName = (TextView) mView.findViewById(R.id.apkName);
            this.mCategorySub = (TextView) mView.findViewById(R.id.categorySub);
            this.mAppSize = (TextView) mView.findViewById(R.id.apkSize);
            this.mRatingBar = (RatingBar) mView.findViewById(R.id.ratingbar_Indicator);
            this.mProgressButton = (Button) mView.findViewById(R.id.list_item_progress_button);
            this.mCategoryListArrow = (ImageView) mView.findViewById(R.id.categoryListArrow);
        }
        public ImplStatusTag getStatusTag() {
            return statusTag;
        }
        public void setStatusTag(ImplStatusTag statusTag) {
            this.statusTag = statusTag;
        }

        public void setmAppIcon(String iconUrl) {
            if (null != this.mAppIcon && null != iconUrl){
                mFinalBitmap.display(this.mAppIcon, iconUrl);
            }else {
                mAppIcon.setImageResource(R.drawable.buffer);
            }
        }

        public void setmAppName(String name) {
            if (null != this.mAppName && null != name) {
                this.mAppName.setText(name);
            }
        }

        public void setCategorySub(String categorySub) {
            if (null != this.mCategorySub && null != categorySub) {
                this.mCategorySub.setText(categorySub);
            }
        }

        public void setAppSize(String size){
            if (null != this.mAppSize && null != size) {
                String mSize = AppliteUtils.bytes2kb(Long.parseLong(size));
                if (null != mSize) {
                    this.mAppSize.setText(mSize);
                }
            }
        }

        public void setmRatingBar(float rating) {
            if (null != mRatingBar) {
                this.mRatingBar.setRating(rating);
            }
        }

        public void setmProgressButton(ImplStatusTag statusTag) {
            if (null != mProgressButton){
                mProgressButton.setTag(statusTag);
                mProgressButton.setText(statusTag.getActionText());
                mProgressButton.setOnClickListener(ListArrayAdapter.this);
            }
        }

        public HomePageApkData getItemData() {
            return itemData;
        }

        public void setItemData(HomePageApkData itemData) {
            this.itemData = itemData;
        }

        public void setImageListArrow(){
            if(null != mCategoryListArrow) {
                mCategoryListArrow.setImageResource(R.drawable.back);
            }
        }
        public String getLayoutStr() {
            return layoutStr;
        }

        public void setLayoutStr(String layoutStr) {
            this.layoutStr = layoutStr;
        }
    }

    public interface ListAdapterListener {
        public void onDownloadButtonClicked(ImplStatusTag tag);
    }
}

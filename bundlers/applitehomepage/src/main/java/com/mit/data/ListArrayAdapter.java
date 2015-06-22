package com.mit.data;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mit.bean.HomePageBean;
import com.mit.bean.HomePageTypeBean;
import com.mit.homepage.BundleContextFactory;
import com.mit.homepage.PullDownView;
import com.mit.homepage.R;
import com.mit.homepage.ScrollOverListView;
import com.mit.impl.ImplAgent;
import com.mit.utils.HomePageUtils;
import com.mit.utils.Utils;
import com.mit.view.ProgressButton;

import net.tsz.afinal.FinalBitmap;

import java.util.List;

/**
 * Created by yuzhimin on 6/17/15.
 */
public class ListArrayAdapter extends ArrayAdapter<HomePageBean> {
    private LayoutInflater mInflater = null;
//        ViewHolder holderOrders = new ViewHolder();
    private static final String TAG = "ListArrayAdapter";
    //        ViewHolder holderMainTypes = new ViewHolder();
    private Context context;
    private int layoutResourceId;
    private List<HomePageBean> mData = null;

    private FinalBitmap mFinalBitmap;
    private int mTable = 0;
    private List<HomePageTypeBean> mDataType = null;
    private PullDownView pullDownView; //PullDown
    private ScrollOverListView listView;

    public ListArrayAdapter(Context context, int resource) {
        super(context, resource);
    }
    public interface ApkItemClickListener {
        void onToOtherFragment();
    }
    public ListArrayAdapter(Context context, int resource, List<HomePageBean> data, List<HomePageTypeBean> dataType, int mTab) {

        super(context, resource, data);
        this.context = context;
        this.layoutResourceId = resource;
        this.mData = data;
        this.mTable = mTab;
        this.mDataType = dataType;
        mFinalBitmap = FinalBitmap.create(context);
        //mData.getClass();
        HomePageUtils.i(TAG, "yuzm-----------------");
        if(null != mData) {
            for (int i = 0; i < mData.size(); i++) {
                HomePageUtils.i(TAG, "ListAdapter.ListAdapter() yuzm mData.get(" + i +
                        ").getName() : " + mData.get(i).getName());
            }
        }
        HomePageUtils.i(TAG, "yuzm-----------------");
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

        //pullDownView = (PullDownView)mInflater.findViewById(R.id.pullDownView);
        //pullDownView.enableAutoFetchMore(true, 0);
        HomePageUtils.i(TAG,"ListAdapter.ListAdapter()");

    }

    @Override
    public int getCount() {
        if(null != mData) {
            HomePageUtils.i(TAG, "ListAdapter.ListAdapter() this.data.size() : " + this.mData.size());
            return this.mData.size();
        }else if (null != mDataType){
            return this.mDataType.size();
        }
        return 0;
    }

    @Override
    public HomePageBean getItem(int position) {
        HomePageUtils.i(TAG,"ListAdapter.getItem() position : " + position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        HomePageUtils.i(TAG,"ListAdapter.getItemId() yuzm position : " + position);
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        float mStaring = 0.0f;
        ViewHolder holderGoods = null;
        HomePageUtils.i(TAG, "ListAdapter.getView convertView : " + convertView);
        //Log.i(TAG, "ListAdapter.getView yuzm convertView : " + convertView, new Throwable());
        if (convertView == null) {
            //HomePageUtils.i(TAG, "ListAdapter.getView container : " + convertView);
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            try {
                Context mContext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
                if (null != mContext) {
                    inflater = LayoutInflater.from(mContext);
                    inflater = inflater.cloneInContext(mContext);
                    //context = mContext;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                convertView = inflater.inflate(layoutResourceId, parent, false);
                holderGoods = new ViewHolder(convertView);
                convertView.setTag(holderGoods);
            }catch(Exception e){
                e.printStackTrace();
            }
        } else {
            holderGoods = (ViewHolder) convertView.getTag();
        }

        HomePageBean item = null;
        HomePageTypeBean itemType =null;
        try {
            if (null != mData) {
                HomePageUtils.i(TAG, "ListAdapter.getView position : " + position);
                item = mData.get(position);
                HomePageUtils.i(TAG, "ListAdapter.getView item : " + item);
            }else if(null != mDataType){
                itemType = mDataType.get(position);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(null != item || null != itemType) {
            try {
                mStaring = Float.parseFloat(item.getRating().toString());
            }catch (Exception e){
                mStaring = 0.0f;
            }
            try {
                switch (mTable){
                    case 0 :
                    case 1 :
                        try {
                            final int mApkType = Utils.isAppInstalled(context, item.getPackagename(), item.getmVersionCode());
                            if (mApkType == Utils.INSTALLED) {
                                holderGoods.mProgressButton.setText("open");
                            } else if (mApkType == Utils.INSTALLED_UPDATE) {
                                holderGoods.mProgressButton.setText("update");
                            } else if (mApkType == Utils.UNINSTALLED) {
                                holderGoods.mProgressButton.setText("install");
                            }
                            final ViewHolder finalViewholder = holderGoods;
                            final HomePageBean finalItem = item;
                            holderGoods.mProgressButton.setOnProgressButtonClickListener(new ProgressButton.OnProgressButtonClickListener() {
                                @Override
                                public void onClickListener() {
                                    if (mApkType == Utils.INSTALLED) {
                                        Utils.startApp(context, finalItem.getPackagename());
                                    } else {
                                        Utils.setDownloadViewText(context, finalViewholder.mProgressButton);
                                        ImplAgent.downloadPackage(context,
                                                finalItem.getPackagename(),
                                                finalItem.getUrl(),
                                                Utils.extenStorageDirPath,
                                                finalItem.getName() + ".apk",
                                                3,
                                                false,
                                                finalItem.getName(),
                                                "",
                                                true,
                                                finalItem.getImgurl(),
                                                "",
                                                finalItem.getPackagename());
                                    }
                                }
                            });
                            mFinalBitmap.display(holderGoods.mAppIcon, item.getImgurl());
                            holderGoods.mAppName.setText(item.getName().toString());
                            holderGoods.mAppSize.setText(item.getCategorySub().toString());
                            holderGoods.mRatingBar.setRating(mStaring / 2.0f);
                        }catch (Exception e){
                            e.printStackTrace();
                            //holderGoods.mAppIcon.setImageResource(R.drawable.buffer);
                        }
                        break;
                    case 2 :
                        //HomePageUtils.i(TAG, "ListAdapter.getView item : " + item);
                        HomePageUtils.i(TAG, "ListAdapter.getView getM_IconUrl : " + itemType.getM_IconUrl());
                        try {
                            if ((null != itemType.getM_IconUrl())) {
                                mFinalBitmap.display(holderGoods.mAppIcon, itemType.getM_IconUrl());
                            }else {
                                holderGoods.mAppIcon.setImageResource(R.drawable.buffer);
                            }
                            convertView.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                            holderGoods.mImageView.setImageResource(R.drawable.back);
                            holderGoods.mAppName.setText(itemType.getM_Name().toString());
                            convertView.findViewById(R.id.textView2).setVisibility(View.GONE);
                            convertView.findViewById(R.id.ratingbar_Indicator).setVisibility(View.GONE);
                            convertView.findViewById(R.id.list_item_progress_button).setVisibility(View.GONE);
                        }catch (Exception e){
                            holderGoods.mAppIcon.setImageResource(R.drawable.buffer);
                            convertView.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                            holderGoods.mImageView.setImageResource(R.drawable.back);
                            holderGoods.mAppName.setText(itemType.getM_Name().toString());
                            convertView.findViewById(R.id.textView2).setVisibility(View.GONE);
                            convertView.findViewById(R.id.ratingbar_Indicator).setVisibility(View.GONE);
                            convertView.findViewById(R.id.list_item_progress_button).setVisibility(View.GONE);
                            e.printStackTrace();
                        }
                        break;
                }

            }catch (Exception e) {
                HomePageUtils.i(TAG, "ListAdapter.getView Exception holderGoods : " + holderGoods);
                e.printStackTrace();
            }
        }else {
            HomePageUtils.i(TAG, "ListAdapter.getView yuzm item : " + item);
        }
        return convertView;
    }

    public void setData(List<HomePageBean> data) {
        this.mData = data;
        this.notifyDataSetChanged();
    }

    class ViewHolder {
        public ImageView mAppIcon;
        public TextView mAppName;
        public TextView mAppSize;
        public RatingBar mRatingBar;
        public Button mAppInstall;
        public ImageView mImageView;
        public ProgressButton mProgressButton;
        ViewHolder(View mView){
            this.mAppIcon = (ImageView) mView.findViewById(R.id.imageViewName);
            this.mAppName = (TextView) mView.findViewById(R.id.textView);
            this.mAppSize = (TextView) mView.findViewById(R.id.textView2);
            this.mRatingBar = (RatingBar) mView.findViewById(R.id.ratingbar_Indicator);
            this.mImageView = (ImageView) mView.findViewById(R.id.imageView);
            this.mProgressButton = (ProgressButton) mView.findViewById(R.id.list_item_progress_button);
        }
    }
}

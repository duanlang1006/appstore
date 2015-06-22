package com.android.applite.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.applite.model.AppLiteModel;
import com.android.applite.model.IAppInfo;
import com.android.applite.model.IModelCallback;


public class AppsOnlinePagedView extends AppsCustomizePagedView {
    private static final String TAG = "AppLite_AppsOnlinePagedView";
    private static boolean DEBUG = false;
    private Context mContext;
    private IModelCallback mCallback;
    private boolean frist=false;
    public AppsOnlinePagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        this.mContext=context;
        mDeferScrollUpdate=true;
    }

    @Override
    public void setApps(final ArrayList<IAppInfo> list) {
        // TODO Auto-generated method stub
        mApps = list;
//        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
//        Collections.sort(mApps, AppLiteModel.APP_ONLINE_COMPARATOR);
        if (DEBUG) Log.d(TAG,"setApps start");
        for (IAppInfo info : mApps){
            info.clearDisplay();
        }

        updatePageCounts();
        if (testDataReady()) {
            requestLayout();
        }
    }
    
    @Override
    public void addApps(ArrayList<IAppInfo> list) {
        // TODO Auto-generated method stub
        if (DEBUG) Log.d(TAG,"addApps");
        boolean dataReady = testDataReady();
        addAppsWithoutInvalidate(list);
        updatePageCounts();
        if (false == dataReady && testDataReady()){
            if (DEBUG) Log.d(TAG,"addApps,requestLayout()");
            mIsDataReady=false;
            requestLayout();
        }else{
            invalidatePageData();
        }
    }

    @Override
    public void removeApps(ArrayList<IAppInfo> list) {
        // TODO Auto-generated method stub
        if (DEBUG) Log.d(TAG,"removeApps");
        removeAppsWithoutInvalidate(list);
        updatePageCounts();
        invalidatePageData();
    }

    @Override
    public void updateApps(ArrayList<IAppInfo> list) {
        // TODO Auto-generated method stub
        if (DEBUG) Log.d(TAG,"updateApps");
        super.updateApps(list);
    }

    @Override
    protected void startRefresh() {
        // TODO Auto-generated method stub
        this.post(new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Collections.sort(mApps, AppLiteModel.APP_ONLINE_COMPARATOR);
                if (DEBUG) Log.d(TAG,"startRefresh start");
                for (IAppInfo info : mApps){
                    info.clearDisplay();
                }
                if (DEBUG) Log.d(TAG,"startRefresh finished");
                updatePageCounts();
                invalidatePageData();
            }
        });
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
//            case R.id.featured_refresh_btn:
//                startRefresh();
//                break;
            default:
                break;
        }
        super.onClick(v);
    }
    
    private void addAppsWithoutInvalidate(ArrayList<IAppInfo> list) {
        // We add it in place, in alphabetical order
        int count = list.size();
        for (int i = 0; i < count; ++i) {
            IAppInfo info = list.get(i);
            int addIndex = findAppById(mApps, info);
            if (addIndex==-1) {
				mApps.add(info);
			}
        }
    }
    private void removeAppsWithoutInvalidate(ArrayList<IAppInfo> list) {
        // loop through all the apps and remove apps that have the same
        // component
        int length = list.size();
        for (int i = 0; i < length; ++i) {
            IAppInfo info = list.get(i);
            int removeIndex = findAppById(mApps, info);
            if (removeIndex > -1) {
                mApps.remove(removeIndex);
            }
        }
    }
    
    private int findAppById(List<IAppInfo> list,IAppInfo item) {
        int length = list.size();
        for (int i = 0; i < length; ++i) {
            IAppInfo info = list.get(i);
            if (info.getId().equals(item.getId())) {
                return i;
            }
        }
        return -1;
    }
    
//    @Override
//    protected void updatePageCounts() {
//        // TODO Auto-generated method stub
//        mNumAppsPages = 1;
//    }
    class GalleryAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mApps.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ImageView mImageView=new ImageView(mContext);
//			BitmapDrawable drawable=new BitmapDrawable(mContext.getResources(), mApps.get(position).getDetailIcon(true));
//			drawable.setAntiAlias(true);
//			mImageView.setImageDrawable(drawable);
			mImageView.setImageURI(Uri.parse(mApps.get(position).getDetailUrl()));
			mImageView.setScaleType(ScaleType.FIT_XY);
			return mImageView;
		}
    	
    }
    public void Selection(IAppInfo appInfo) {
    	int index=mApps.indexOf(appInfo);
	}

	public void setmGallery(GalleryFlow Gallery) {
//		this.mGallery = Gallery;
//		mGallery.setSpacing(-20);
//		mGalleryAdapter=new GalleryAdapter();
//		mGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//			@Override
//			public void onItemSelected(AdapterView<?> parent, View view,
//					int position, long id) {
//				// TODO Auto-generated method stub
//				if (mCallback!=null) 
//					mCallback.wakeDetail(mApps.get(position));
//				if (position % mCellCountX == 0	|| position % mCellCountX == mCellCountX - 1) {
//					if (frist) 
//						snapToPage(position / mCellCountX);
//				}
//				frist=true;
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> parent) {
//				// TODO Auto-generated method stub
//
//			}
//
//		});
	}
	public void Callback(IModelCallback callback) {
		mCallback=callback;
	}

	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		if (!mDeferScrollUpdate){
			super.computeScroll();
		}else{
            boolean scrollComputed = computeScrollHelper();

            if (!scrollComputed && mTouchState == TOUCH_STATE_SCROLLING) {
                final float dx = mTouchX - mUnboundedScrollX;
                scrollBy(Math.round(dx), getScrollY());
                if (dx > 1.f || dx < -1.f) {
                    invalidate();
                }
            }

		}
	}
	
	
}

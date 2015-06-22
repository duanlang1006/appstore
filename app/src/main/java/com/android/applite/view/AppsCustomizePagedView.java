package com.android.applite.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.applite.model.AppLiteModel;
import com.android.applite.model.IAppInfo;
import com.android.applite.plugin.IAppLiteOperator;
import com.applite.android.R;

public class AppsCustomizePagedView extends PagedViewWithHeaderAndFooter implements IAppsView, View.OnClickListener, View.OnKeyListener,
		View.OnLongClickListener, View.OnTouchListener {
    private static final boolean DEBUG = false;
	private static final String TAG = "AppLite_AppsCustomizePagedView";

	public static final float PANEL_BIT_DEPTH = 24;
	public static final float ALPHA_THRESHOLD = 0.5f / PANEL_BIT_DEPTH;

	private IAppLiteOperator mOperator;
	private final LayoutInflater mLayoutInflater;
	long timeDifference;
	// Save and Restore
	private int mSaveInstanceStateItemIndex = -1;

	protected ArrayList<IAppInfo> mApps;

	// Dimens
	private PagedViewCellLayout mWidgetSpacingLayout;
	private int mContentWidth;
	private int mMaxAppCellCountX, mMaxAppCellCountY;
	private boolean isInstall;
	public int mNumAppsPages;
	
	// Previews & outlines
//	private HolographicOutlineHelper mHolographicOutlineHelper;

	// Save and Restore
	private boolean removeFlag = false;
	private Context mContext;
	private LinearLayout mLinearLayout;
	public AppsCustomizePagedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		context.getPackageManager();
		mApps = new ArrayList<IAppInfo>();
//		mHolographicOutlineHelper = new HolographicOutlineHelper();

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.AppsCustomizePagedView, 0, 0);
		mMaxAppCellCountX = a.getInt(
				R.styleable.AppsCustomizePagedView_maxAppCellCountX, -1);
		mMaxAppCellCountY = a.getInt(
				R.styleable.AppsCustomizePagedView_maxAppCellCountY, -1);
		isInstall=a.getBoolean(R.styleable.AppsCustomizePagedView_isInstall, false);
		a.recycle();
		mWidgetSpacingLayout = new PagedViewCellLayout(mContext);
	}
	

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		super.init();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		if (!isDataReady()) {
			if (testDataReady()) {
				setDataIsReady();
				// 给这个view设置宽和高
				setMeasuredDimension(width, height);
				onDataReady(width, height);
			}
		}
		if (Panel.reLayoutFlag&&isInstall) {
			if (testDataReady()/*&&mApps.size()>mCellCountX * mCellCountY*/) {
				Panel.reLayoutFlag = false;
				onDataReady(width, height);
			}
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		final IAppInfo appInfo = (IAppInfo) v.getTag();
		if (v instanceof CustomizedPagedViewIcon) {
			// Animate some feedback to the click
			animateClickFeedback(v, new Runnable() {
				@Override
				public void run() {
				    switch(appInfo.getItemType()){
				        case IAppInfo.AppInstalled:
				        case IAppInfo.AppMore:
				            if(!removeFlag){
				                mOperator.startActivitySafely(appInfo.getIntent(), appInfo);				                
				            }
				            break;
				        case IAppInfo.AppOffline:
				            mOperator.onOfflineAppClick(appInfo);
				            break;
				        default:
				            mOperator.onOnlineAppClick(appInfo);
				            break;
				    }
				}
			});
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

	@Override
	public boolean onLongClick(View v) {
		Vibrator vibrator = (Vibrator) mContext
				.getSystemService(Context.VIBRATOR_SERVICE);
		mContext.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(140);
		if (v instanceof CustomizedPagedViewIcon) {
			final IAppInfo appInfo = (IAppInfo) v.getTag();
			if (appInfo.getItemType() != IAppInfo.AppOnline
			        &&appInfo.getItemType() != IAppInfo.AppMore) {
				animateClickFeedback(v, new Runnable() {
					@Override
					public void run() {
					    setRemoveMode(true);
//						mOperator.onAppOnLongClick();
					}
				});
			} else {
				return false;
			}
		}

		return true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	public void onResume() {
		if (getVisibility() == VISIBLE) {
			loadAssociatedPages(getCurrentPage());
		}
	}

	/*
	 * Apps PagedView implementation
	 */
	private void setVisibilityOnChildren(ViewGroup layout, int visibility) {
		int childCount = layout.getChildCount();
		for (int i = 0; i < childCount; ++i) {
			layout.getChildAt(i).setVisibility(visibility);
		}
	}

	@Override
	public void syncPages() {
		// TODO Auto-generated method stub
	    final long t = DEBUG?SystemClock.uptimeMillis():0;
		removeAllViews();

//		Context context = getContext();
		for (int i = 0; i < mNumAppsPages; ++i) {
			PagedViewCellLayout layout = new PagedViewCellLayout(mContext);
			setupPage(layout);
			addView(layout);
		}
		if (DEBUG){
            Log.d(TAG,"syncPages,cost:"+(SystemClock.uptimeMillis()-t)+" ms");
        }
	}

	@Override
	protected void onUnhandledTap(MotionEvent ev) {
		// TODO Auto-generated method stub
		super.onUnhandledTap(ev);
	}

	@Override
	public void syncPageItems(int page, boolean immediate) {
		// TODO Auto-generated method stub
	    final long t = DEBUG?SystemClock.uptimeMillis():0;
		if (page < mNumAppsPages) {
			syncAppsPageItems(page, immediate);
		}
		if (DEBUG){
            Log.d(TAG,"syncPageItems("+page+","+immediate+"),cost:"+(SystemClock.uptimeMillis()-t)+" ms");
        }
	}

	View getPageAt(int index) {
		return getChildAt(getChildCount() - index - 1);
	}

	@Override
	protected int indexToPage(int index) {
		return getChildCount() - index - 1;
	}

	protected void overScroll(float amount) {
		acceleratedOverScroll(amount);
	}

	/**
	 * Used by the parent to get the content width to set the tab bar to
	 * @return
	 */
	public int getPageContentWidth() {
		return mContentWidth;
	}
	
    void enableChildrenCache(int fromPage, int toPage) {
        if (fromPage > toPage) {
            final int temp = fromPage;
            fromPage = toPage;
            toPage = temp;
        }

        final int screenCount = getChildCount();

        fromPage = Math.max(fromPage, 0);
        toPage = Math.min(toPage, screenCount - 1);

        for (int i = fromPage; i <= toPage; i++) {
            final PagedViewCellLayout layout = (PagedViewCellLayout) getPageAt(i);
            layout.setChildrenDrawnWithCacheEnabled(true);
            layout.setChildrenDrawingCacheEnabled(true);
        }
    }

    void clearChildrenCache() {
        final int screenCount = getChildCount();
        for (int i = 0; i < screenCount; i++) {
            final PagedViewCellLayout layout = (PagedViewCellLayout) getPageAt(i);
            layout.setChildrenDrawnWithCacheEnabled(false);
            layout.setChildrenDrawingCacheEnabled(false);
        }
    }

	@Override
    protected void onPageBeginMoving() {
        // TODO Auto-generated method stub
        super.onPageBeginMoving();
        if (mNextPage != INVALID_PAGE) {
            // we're snapping to a particular screen
            enableChildrenCache(mCurrentPage, mNextPage);
        } else {
            // this is when user is actively dragging a particular screen, they might
            // swipe it either left or right (but we won't advance by more than one screen)
            enableChildrenCache(mCurrentPage - 1, mCurrentPage + 1);
        }
    }

    @Override
	protected void onPageEndMoving() {
		super.onPageEndMoving();

		clearChildrenCache();
		// We reset the save index when we change pages so that it will be
		// recalculated on next
		// rotation
		mSaveInstanceStateItemIndex = -1;
	}

	private void addAppsWithoutInvalidate(ArrayList<IAppInfo> list) {
		// We add it in place, in alphabetical order
		int count = list.size();
		for (int i = 0; i < count; ++i) {
			IAppInfo info = list.get(i);
			//binarySearch之前mApps必须sort
			int index = Collections.binarySearch(mApps,info,AppLiteModel.APP_NAME_COMPARATOR);
			if (index < 0) {
				mApps.add(-(index + 1), info);
			}
		}
	}

	private int findAppByComponent(List<IAppInfo> list,IAppInfo item) {
		ComponentName removeComponent = item.getComponentName();
		/*
		 * Intent.getComponent(),intent是数据库中app的intent,getComponent()得到ComponentName,可以通过componentName获取包名和类名
		 * (可以实现通过setComponent启动不同的应用程序,并在另一个应用程序中获取传递的消息)
		 */
		//String pkgName = removeComponent.getPackageName();  
		//String className = removeComponent.getClassName();  
		int length = list.size();
		for (int i = 0; i < length; ++i) {
			IAppInfo info = list.get(i);
			if (info.getComponentName().equals(removeComponent)) {
				return i;
			}
		}
		return -1;
	}

	public void removeAppsWithoutInvalidate(IAppInfo info) {
		int removeIndex = findAppByComponent(mApps, info);
		if (removeIndex > -1) {
			mApps.remove(removeIndex);
		}
	}

	private void removeAppsWithoutInvalidate(ArrayList<IAppInfo> list) {
		// loop through all the apps and remove apps that have the same
		// component
		int length = list.size();
		for (int i = 0; i < length; ++i) {
			IAppInfo info = list.get(i);
			int removeIndex = findAppByComponent(mApps, info);
			if (removeIndex > -1) {
				mApps.remove(removeIndex);
			}
		}
	}
	/**
	 * This differs from isDataReady as this is the test done if isDataReady is
	 * not set.
	 */
	protected boolean testDataReady() {
		// We only do this test once, and we default to the Applications page,
		// so we only really
		// have to wait for there to be apps.
		// TODO: What if one of them is validly empty
		return !mApps.isEmpty();
	}
	public void setLinearLayout(LinearLayout linearLayout) {
		this.mLinearLayout=linearLayout;
	}
	protected void updatePageCounts() {
		mNumAppsPages = (int) Math.ceil((float) mApps.size()
				/ (mCellCountX * mCellCountY));
		if (mNumAppsPages==0) {
			mNumAppsPages = 1;
		}
		if (mNumAppsPages ==1) {
			if (mLinearLayout!=null) {
				mLinearLayout.setVisibility(LinearLayout.VISIBLE);
			}
		}
	}
	/**
	 * 计算占用的页数,内容的宽度,细胞的数量,以更新重新计算的差距,存储页面
	 * @param width
	 * @param height
	 */
	protected void onDataReady(int width, int height) {
		// Now that the data is ready, we can calculate the content width, the
		// number of cells to
		// use for each page
		// 检测屏幕的方向：纵向或横向
		// Configuration.ORIENTATION_LANDSCAPE 横屏
		// /Configuration.ORIENTATION_PORTRAIT 竖屏
		boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		int maxCellCountX = Integer.MAX_VALUE;
		int maxCellCountY = Integer.MAX_VALUE;
//		 if (AppLiteApplication.isScreenLarge()) {
//		 maxCellCountX = (isLandscape ? AppLiteModel.getCellCountX() : AppLiteModel.getCellCountY());
//		 maxCellCountY = (isLandscape ? AppLiteModel.getCellCountY() : AppLiteModel.getCellCountX());
//		 }
		if (mMaxAppCellCountX > -1) {
			maxCellCountX = Math.min(maxCellCountX, mMaxAppCellCountX);
		}
		if (mMaxAppCellCountY > -1) {
			maxCellCountY = Math.min(maxCellCountY, mMaxAppCellCountY);
		}

		// Now that the data is ready, we can calculate the content width, the
		// number of cells to
		// use for each page
		mWidgetSpacingLayout.setGap(mPageLayoutWidthGap, mPageLayoutHeightGap);
		mWidgetSpacingLayout.setPadding(mPageLayoutPaddingLeft,	mPageLayoutPaddingTop, mPageLayoutPaddingRight,	mPageLayoutPaddingBottom);
		mWidgetSpacingLayout.calculateCellCount(width, height, maxCellCountX,maxCellCountY);
		mCellCountX = mWidgetSpacingLayout.getCellCountX();
		mCellCountY = mWidgetSpacingLayout.getCellCountY();
		updatePageCounts();

		// Force a measure to update recalculate the gaps
		int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(),	MeasureSpec.AT_MOST);
		int heightSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(),MeasureSpec.AT_MOST);
		mWidgetSpacingLayout.measure(widthSpec, heightSpec);
		mContentWidth = mWidgetSpacingLayout.getContentWidth();

		// Restore the page
		int page = getPageForComponent(mSaveInstanceStateItemIndex);
		invalidatePageData(Math.max(0, page), false);
	}

	/**
	 * Returns the page in the current orientation which is expected to contain
	 * the specified item index.
	 */
	int getPageForComponent(int index) {
		if (index < 0)
			return 0;
		int numItemsPerPage = mCellCountX * mCellCountY;
		return (index / numItemsPerPage);
	}

	private void setupPage(PagedViewCellLayout layout) {
		layout.setCellCount(mCellCountX, mCellCountY);
		layout.setGap(mPageLayoutWidthGap, mPageLayoutHeightGap);
		layout.setPadding(mPageLayoutPaddingLeft, mPageLayoutPaddingTop,mPageLayoutPaddingRight, mPageLayoutPaddingBottom);

		// Note: We force a measure here to get around the fact that when we do
		// layout calculations
		// immediately after syncing, we don't have a proper width. That said,
		// we already know the
		// expected page width, so we can actually optimize by hiding all the
		// TextView-based
		// children that are expensive to measure, and let that happen naturally
		// later.
		setVisibilityOnChildren(layout, View.GONE);
		int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(),
				MeasureSpec.AT_MOST);
		int heightSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(),
				MeasureSpec.AT_MOST);
		layout.setMinimumWidth(getPageContentWidth());
		layout.measure(widthSpec, heightSpec);
		setVisibilityOnChildren(layout, View.VISIBLE);
	}

	private void syncAppsPageItems(int page, boolean immediate) {
		// ensure that we have the right number of items on the pages
		int numCells = mCellCountX * mCellCountY;
		int startIndex = page * numCells;
		int endIndex = Math.min(startIndex + numCells, mApps.size());
		PagedViewCellLayout layout = (PagedViewCellLayout) getPageAt(page);
		if (isInstall&&endIndex==0) {
			removeFlag=false;
		}
		layout.removeAllViewsOnPage();
		CustomizedPagedViewIcon view = null;
		for (int i = startIndex; i < endIndex; ++i) {
		    final long t = DEBUG?SystemClock.uptimeMillis():0;
		    
			IAppInfo info = mApps.get(i);
			view = (CustomizedPagedViewIcon) mLayoutInflater.inflate(
					R.layout.apps_customize_application, layout, false);
			view.initView();
			view.applyFromApplicationInfo(info, true, /*mHolographicOutlineHelper,*/removeFlag);
			view.setOperator(mOperator);
			view.setOnClickListener(this);
			view.setOnLongClickListener(this);
			view.setOnTouchListener(this);
			view.setOnKeyListener(this);
			view.setTag(info);
			int index = i - startIndex;
			int x = index % mCellCountX;
			int y = index / mCellCountX;
			// 加载每个icon到view的哪个位置
			layout.addViewToCellLayout(view, -1, i,new PagedViewCellLayout.LayoutParams(x, y, 1, 1));
//			mOperator.onShowIndication(info);
			// items.add(info);
			// images.add(info.getIcon());
			
			if (DEBUG){
	            Log.d(TAG,"syncAppsPageItems("+i+"),cost:"+(SystemClock.uptimeMillis()-t)+" ms");
	        }
		}
		layout.createHardwareLayers();
		/*
		 * TEMPORARILY DISABLE HOLOGRAPHIC ICONS if (mFadeInAdjacentScreens) {
		 * prepareGenerateHoloOutlinesTask(page, items, images); }
		 */
	}

	// IAppsView interface
	@Override
	public void setup(IAppLiteOperator operator) {
		// TODO Auto-generated method stub
		mOperator = operator;
	}

	@Override
	public void zoom(float zoom, boolean animate) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAnimating() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setApps(ArrayList<IAppInfo> list) {
		// TODO Auto-generated method stub
	    if (DEBUG) Log.d(TAG,"setApps start");
		mApps = list;
		Collections.sort(mApps, AppLiteModel.APP_NAME_COMPARATOR);
		updatePageCounts();
		// The next layout pass will trigger data-ready if both widgets and apps
		// are set, so
		// request a layout to do this test and invalidate the page data when
		// ready.
		if (testDataReady()) {
			requestLayout();
		}
	}

	@Override
	public void addApps(ArrayList<IAppInfo> list) {
		// TODO Auto-generated method stub
	    if (DEBUG)  Log.d(TAG,"addApps start");
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
//		invalidatePageData();
	}

	@Override
	public void removeApps(ArrayList<IAppInfo> list) {
		// TODO Auto-generated method stub
	    if (DEBUG) Log.d(TAG,"removeApps start");
		removeAppsWithoutInvalidate(list);
		updatePageCounts();
		invalidatePageData();
	}

	@Override
	public void updateApps(ArrayList<IAppInfo> list) {
		// TODO Auto-generated method stub
//		Collections.sort(mApps, AppLiteModel.APP_NAME_COMPARATOR);
//		if (DEBUG) Log.d(TAG,"updateApps start");
//	    for (int i = 0;i<getPageCount();i++){
//            IPage layout = (IPage) getPageAt(mCurrentPage);
//            if (null == layout) continue;
//            for (int j = 0; j < layout.getPageChildCount(); j++) {
//                final CustomizedPagedViewIcon v = (CustomizedPagedViewIcon) layout.getChildOnPageAt(j);
//                IAppInfo info = (IAppInfo) v.getTag();
//                if (list.contains(info)) {
//                    PagedViewIcon mPagedViewIcon=(PagedViewIcon) v.findViewById(R.id.application_icon);
//                    mPagedViewIcon.update(i==mCurrentPage);
//                }
//            }
//	    }
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		if (mCurrentPage != 0) {
			invalidatePageData(0);
		}
	}

	@Override
	public void dumpState() {
		// TODO Auto-generated method stub

	}

	@Override
	public void surrender() {
		// TODO Auto-generated method stub

	}


	@Override
	protected void startRefresh() {
		// TODO Auto-generated method stub
	}


	public boolean getRemoveMode() {
		return removeFlag;
	}


	public void setRemoveMode(boolean flag) {
		if (removeFlag!=flag) {
			removeFlag=flag;
			invalidatePageData();
		}
	}
	
	public void forceInit(){
	    init();
	    mApps.clear();
	    updatePageCounts();
	    mIsDataReady = false;
	    removeAllViews();
	}
}

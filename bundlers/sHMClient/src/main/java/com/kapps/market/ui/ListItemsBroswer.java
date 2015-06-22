package com.kapps.market.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * 2011-3-11<br>
 * ��Ҫ��������һ��ListView<br>
 * �б�����ͼ��������addListHeader����һ��ͷ���Ŀշ����� ���������Ҫ��ͷ����ʵ����
 * 
 */
public abstract class ListItemsBroswer extends BaseItemList {

	// ʼ��ʼ�б�߶�Ϊ�б��е����ܳ���һ��
	private boolean fixHeight;

	public ListItemsBroswer(Context context) {
		super(context);
	}

	public ListItemsBroswer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListItemsBroswer(Context context, boolean useLongClick) {
		super(context, useLongClick);
	}

	/**
	 * 
	 * @param context
	 * @param useLongClick
	 * @param fixHeight
	 *            �Ƿ�ʼ�ձ����б?�����������һ��
	 */
	public ListItemsBroswer(Context context, boolean useLongClick,
			boolean fixHeight) {
		super(context, useLongClick);
		this.fixHeight = fixHeight;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// �ų�ͷ����β��
		int hfCount = ((ListView) view).getHeaderViewsCount()
				+ ((ListView) view).getFooterViewsCount();
		// Log.v(TAG, "visibleItemCount: " + visibleItemCount + " \nhfCount: " +
		// hfCount);
		if (!firstTriggerLoadIconOver && visibleItemCount > hfCount) {
			firstTriggerLoadIconOver = true;
			onScrollStateChanged(view, OnScrollListener.SCROLL_STATE_IDLE);
		}
	}

	@Override
	protected void preHandleItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		int headerCount = ((ListView) parent).getHeaderViewsCount();
		if ((position >= headerCount)
				&& (position < getMAdapter().getCount() + headerCount)) {
			Object item = getMAdapter().getItem(position - headerCount);
			if (isNeedDispatchItemClick(item)) {
				handleRealItemClick(item);
			}
		}
	}

	@Override
	protected void preHandleItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		int headerCount = ((ListView) parent).getHeaderViewsCount();
		if ((position >= headerCount)
				&& (position < getMAdapter().getCount() + headerCount)) {
			Object item = getMAdapter().getItem(position - headerCount);
			if (isNeedDispatchItemClick(item)) {
				handleRealItemLongClick(item);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hiapk.market.ui.LoadableList#createAdapterView()
	 */
	@Override
	protected AdapterView createAdapterView() {
		ListView listView = null;
		if (fixHeight) {
			listView = new FixListView(getContext());
			setListViewParameter(listView);

		} else {
			listView = new ListView(getContext());
			listView.setVerticalFadingEdgeEnabled(true);
			setListViewParameter(listView);
		}

        listView.setDividerHeight(1);
		// ���ͷ��
		addListHeader(listView);

		return listView;
	}

	/**
	 * ����Ĭ�ϵ�ListView�ķ��Ͳ������಻Ҫ��������ʵ��
	 */
	protected void setListViewParameter(ListView listView) {
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setDividerHeight(0);
		listView.setFadingEdgeLength(0);
	}

	/**
	 * ���List��ͷ������
	 * 
	 * @param listView
	 */
	protected void addListHeader(ListView listView) {
		// ��ʵ�֣�����Ҫͷ���Լ����ǡ�
	}

	/**
	 * �Ƴ��б�ͷ��
	 * 
	 * @param header
	 */
	public void removeListHeader(View header) {
		((ListView) adapterView).removeHeaderView(header);
	}

	/**
	 * ֻΪ���������б����<br>
	 * ����֮�ã���ʵ��
	 */
	protected void initItemBg(View convertView, int position) {
        /*
		if (position % 2 == 0) {
			convertView.setBackgroundResource(R.drawable.list_item_even_bg);

		} else {
			convertView.setBackgroundResource(R.drawable.list_item_odd_bg);
		}*/

	}

	/**
	 * ���б���ͼ�߶�ʼ��ʼΪ�б��е����ܳ���<br>
	 * ������ڷ�ҳ������ע���ҳ�߼�ȷ�����ᷢ����Ч���ص����� <br>
	 * 2011-3-19
	 * 
	 * @author admin
	 * 
	 */
	private class FixListView extends ListView {
		// ����߶�
		private int childHeight;

		public FixListView(Context context) {
			super(context);
			setFadingEdgeLength(0);
			setHorizontalScrollBarEnabled(false);
			setVerticalScrollBarEnabled(false);
		}

		/**
		 * ������ʾ�����ڵĵ�һ��
		 */
		@Override
		public int getFirstVisiblePosition() {
			Rect rect = new Rect();
			getLocalVisibleRect(rect);
			int first = rect.top / (childHeight == 0 ? 1 : childHeight);
			return first < 0 ? 0 : first;
		}

		@Override
		public int getLastVisiblePosition() {
			Rect rect = new Rect();
			getLocalVisibleRect(rect);
			int last = rect.bottom / (childHeight == 0 ? 1 : childHeight);
			last = last < getCount() ? last : last - 1;
			return last < 0 ? 0 : last;
		}

		private void measureScrapChild(View child, int widthMeasureSpec) {
			AbsListView.LayoutParams p = (AbsListView.LayoutParams) child
					.getLayoutParams();
			if (p == null) {
				p = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT, 0);
				child.setLayoutParams(p);
			}

			int childWidthSpec = ViewGroup.getChildMeasureSpec(
					widthMeasureSpec, 0, p.width);
			int lpHeight = p.height;
			int childHeightSpec;
			if (lpHeight > 0) {
				childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
						MeasureSpec.EXACTLY);
			} else {
				childHeightSpec = MeasureSpec.makeMeasureSpec(0,
						MeasureSpec.UNSPECIFIED);
			}
			child.measure(childWidthSpec, childHeightSpec);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int itemCount = 0, heightSize = 0;
			View itemView = null, headerView = null;
			Adapter adapter = getAdapter();
			if (adapter instanceof HeaderViewListAdapter) {
				HeaderViewListAdapter headerAdapter = (HeaderViewListAdapter) adapter;
				itemCount = headerAdapter.getCount()
						- headerAdapter.getHeadersCount();
				if (headerAdapter.getHeadersCount() > 0) {
					headerView = headerAdapter.getView(0, null, this);
					if (itemCount > 0) {
						itemView = headerAdapter.getView(
								headerAdapter.getHeadersCount(), null, this);
					}

				} else if (itemCount > 0) {
					itemView = headerAdapter.getView(0, null, this);
				}

			} else if (adapter != null) {
				itemCount = adapter.getCount();
				if (itemCount > 0) {
					itemView = adapter.getView(0, null, this);
				}
			}

			// ����Ϊ�������ܳ�
			if (itemView != null) {
				measureScrapChild(itemView, widthMeasureSpec);
				// �ָ���
				Drawable divider = getDivider();
				childHeight = itemView.getMeasuredHeight();
				heightSize = (childHeight + (divider == null ? 0 : divider
						.getIntrinsicHeight())) * itemCount;
				if (headerView != null) {
					headerView.measure(widthMeasureSpec, heightMeasureSpec);
					heightSize += headerView.getMeasuredHeight();
				}
			}

			if (heightSize > 0) {
				super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
						heightSize, MeasureSpec.EXACTLY));

			} else {
				super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			}
		}
	}

}

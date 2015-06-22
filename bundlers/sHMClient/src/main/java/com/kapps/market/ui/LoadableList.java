package com.kapps.market.ui;

import android.content.Context;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kapps.market.R;
import com.kapps.market.log.LogUtil;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppImageTaskMark;
import com.kapps.market.task.mark.AppListTaskMark;

/**
 * 2010-6-23<br>
 * ����صĸ���,�ṩ������ʾ�� �ⲿʹ��ʱ��ͬtaskMark<br>
 * ��Ӧ��ͬ��LoadableList�Ա�����ͼ״̬�ĸ��š�
 * 
 * @author admin
 * 
 */
public abstract class LoadableList extends CommonView implements IResultReceiver {

	public static final String TAG = "LoadableList";
	// ״̬
	protected ATaskMark mTaskMark;
	// ��ͼid
	public static final int LOAD_LIST_ID = 0x01111;
	// �����
	private static final int MM_UPDATE_LOAD_STATE = 491111;
	// ����б���ͼ
	protected AdapterView adapterView;
	// ������ʾ �Զ���ʼ����
	private StatusFrame statusView;
	private boolean mAutoLoad = true;
	private boolean useLongClick;
	// ���һ����ʾ״̬�������ظ����á�
	private ViewStatus lastNoteState = ViewStatus.OVER;
	private int lastLoadNoteId = -49;

	/**
	 * @param context
	 */
	public LoadableList(Context context) {
		super(context);
	}

	/**
	 * @param context
	 */
	public LoadableList(Context context, boolean useLongClick) {
		super(context);
		this.useLongClick = useLongClick;
	}

	public LoadableList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void initLoadleList() {
		initLoadleList(null, true, true, false);
	}

	public void initLoadleList(ATaskMark taskWraper) {
		initLoadleList(taskWraper, true, true, false);
	}

	public void initLoadleList(ATaskMark taskWraper, boolean loadInit) {
		initLoadleList(taskWraper, true, loadInit, false);
	}

	@Override
	public void flushView(int what) {
		super.flushView(what);
		notifyDataSetChanged();
	}

	/**
	 * auto load datas by taskmark, bind view and task together? very interesting..
	 * 
	 * @param taskMark
	 * @param autoLoad
	 *            �Զ�����
	 * @param loadInit
	 *            ��ʼ����
	 * @param forceLoad
	 *            ǿ�Ƽ��� ,����tryQueryNewItemsһ���ᱻִ��
	 */
	public void initLoadleList(ATaskMark taskMark, boolean autoLoad, boolean loadInit, boolean forceLoad) {
		// ��ͬ������ʱ�ؽ��б?
		if (adapterView == null || (taskMark == null || mTaskMark != taskMark)) {
			// ��ʼ������
			mTaskMark = taskMark;
			// �ȳ�ʼ����ͼ
			initView(getContext());

		} else {
			// ��ʼ������
			mTaskMark = taskMark;
		}

		mAutoLoad = autoLoad;
		lastNoteState = ViewStatus.OVER;

		// ��û����ݵ�����£�������������Զ�������ݡ�
		LogUtil.d(TAG, "%%%% \n" + " loadInit: " + loadInit + " count: " + getMAdapter().getCount() + "\n forceload: "
				+ forceLoad + " \ntaskmark: " + mTaskMark);
		if (loadInit && mTaskMark != null && (getMAdapter().getCount() == 0 || forceLoad)) {
			tryQueryNewItems();

		} else {
			updateViewStatus(mTaskMark);
		}
		
		notifyDataSetChanged();
	}

	// ��ʼ��
	protected void initView(Context context) {
		// �������
		removeAllViews();

		// Ӧ���б�
		adapterView = createAdapterView();
		adapterView.setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);

		adapterView.setId(LOAD_LIST_ID);
		adapterView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				preHandleItemClick(parent, view, position, id);
			}

		});

		// �ַ�ʹ�ó���
		if (useLongClick) {
			adapterView.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					preHandleItemLongClick(parent, view, position, id);
					return true;
				}
			});
		}

		// ������ͼ
		View topView = createTopFrame(context);
		if (topView != null) {
			addView(topView, new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		}

		// ������ͼ
		LinearLayout.LayoutParams linerLayoutParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, 0);
		linerLayoutParams.weight = 1;
		addView(adapterView, linerLayoutParams);

		// ״̬��ʾ��ͼ
		statusView = createStatusFrame(context);
		if (statusView != null) {
			addView(statusView, new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		}

		// �ײ���ͼ
		View bottomView = createBottomFrame(context);
		if (bottomView != null) {
			addView(bottomView, new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		}

		// adapter ��߱������ӣ�Ҳ����������˳�����
		BaseAdapter listAdapter = createItemAdapter();
		adapterView.setAdapter(listAdapter);
	}

	/**
	 * ���Ԥ���?��������ͼ���ܲ�ͬ������ListViewҪ����ͷ������ȡ�
	 * 
	 * @param parent
	 * @param view
	 * @param position
	 * @param id
	 */
	protected void preHandleItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position < getMAdapter().getCount()) {
			Object item = getMAdapter().getItem(position);
			if (isNeedDispatchItemClick(item)) {
				handleRealItemClick(item);
			}
		}
	}

	/**
	 * ���Ԥ���?������������ͼ���ܲ�ͬ������ListViewҪ����ͷ������ȡ�
	 * 
	 * @param parent
	 * @param view
	 * @param position
	 * @param id
	 */
	protected void preHandleItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (position < getMAdapter().getCount()) {
			Object item = getMAdapter().getItem(position);
			if (isNeedDispatchItemClick(item)) {
				handleRealItemLongClick(item);
			}
		}
	}

	/**
	 * ��������б����ܹ�������<br>
	 * �ⷵ�ص�������û�а���ͷ����β��
	 * 
	 * @return
	 */
	public int getDataItemCount() {
		Adapter adapter = getMAdapter();
		if (adapter != null) {
			return adapter.getCount();
		} else {
			return -1;
		}
	}

	/**
	 * ģ���б�İ�������,��������positionΪ�����˵���ǿյ���ݣ�����null<br>
	 * �����ж�handleRealItemClick(null);Ϊ��ʱ�Ĵ���
	 * 
	 * @param position
	 */
	public void simulateOnItemClick(int position) {
		if (position < getMAdapter().getCount() && position >= 0) {
			Object item = getMAdapter().getItem(position);
			if (isNeedDispatchItemClick(item)) {
				handleRealItemClick(item);
			}

		} else if (position < 0) {
			handleRealItemClick(null);
		}

	}

	/**
	 * ������ʾ�ı�
	 * 
	 * @param noteId
	 */
	public void updateLoadNote(int noteId) {
		if (lastLoadNoteId != noteId) {
			lastLoadNoteId = noteId;
			TextView textView = (TextView) statusView.findViewById(R.id.progressLabel);
			textView.setText(getContext().getString(lastLoadNoteId));
		}
	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		// ͳһ��ʾ, ͼƬ������ʾ
		if (taskMark.getTaskStatus() == ATaskMark.HANDLE_ERROR) {
			handleResultError(taskMark, exception, trackerResult);
		}
        else if (taskMark instanceof AppListTaskMark) {
            Log.d("temp", "i am category list");
        }
	}

	// ���?�������
	protected void handleResultError(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		if (!(taskMark instanceof AppImageTaskMark)) {
			Toast.makeText(getContext(), exception.getExMessage(), 150).show();
		}
	}

	protected void updateViewStatus(ATaskMark aTaskMark) {
		LogUtil.d(TAG, "%%% updateViewStatus: aTaskMark - " + aTaskMark);
		if (aTaskMark == null) {
			updateViewStatus(ViewStatus.OVER);
			return;
		}

		switch (aTaskMark.getTaskStatus()) {
		case ATaskMark.HANDLE_OVER:
			updateViewStatus(aTaskMark, ViewStatus.OVER);
			break;

		case ATaskMark.HANDLE_DOING:
			updateViewStatus(aTaskMark, ViewStatus.DOING);
			break;

		case ATaskMark.HANDLE_WAIT:
			updateViewStatus(aTaskMark, ViewStatus.WAIT);
			break;

		case ATaskMark.HANDLE_ERROR:
			updateViewStatus(aTaskMark, ViewStatus.ERROR);
			break;

		default:
			throw new IllegalArgumentException("unknow status taskMark: " + aTaskMark);
		}
	}

	/**
	 * �����״ֻ̬����ʵ�ʵ����������������ͼ״̬��ʵ�ʵĲ������������<br>
	 * ���ȷ�������Ƕ�����
	 * 
	 * @param aTaskMark
	 *            ��ʾ��ǰ�޸�״̬������һ������
	 * @param viewStatus
	 *            ϣ�����õ���ͼ״̬
	 */
	protected void updateViewStatus(ATaskMark aTaskMark, ViewStatus viewStatus) {
		if (lastNoteState == viewStatus && mTaskMark == aTaskMark) {
			return;

		} else {
			lastNoteState = viewStatus;
		}
		sendEmptyQueueMessage(MM_UPDATE_LOAD_STATE);
	}

	// �ڲ�ʹ��
	private void updateViewStatus(ViewStatus viewStatus) {
		lastNoteState = viewStatus;
		sendEmptyQueueMessage(MM_UPDATE_LOAD_STATE);
	}

	// ʵ�ʵ��޸�
	private void doUpdataViewStatus() {
		if (statusView == null) {
			return;
		}

		switch (lastNoteState) {
		case DOING:
			statusView.showLoadView();
			break;

		case ERROR:
			statusView.showErrorView();
			break;

		case WAIT:
			statusView.showWaitView();
			break;

		case OVER:
			statusView.hideNoteView();
			break;

		default:
			throw new IllegalArgumentException("unknow status lastNoteState: " + lastNoteState);
		}
	}

	@Override
	public void handleChainMessage(Message message) {
		switch (message.what) {
		case MM_UPDATE_LOAD_STATE:
			doUpdataViewStatus();
			break;

		default:
			break;
		}
	}

	/**
	 * @return the mAutoLoad
	 */
	public boolean isAutoLoad() {
		return mAutoLoad;
	}

	/**
	 * @param mAutoLoad
	 *            the mAutoLoad to set
	 */
	public void setAutoLoad(boolean autoLoad) {
		this.mAutoLoad = autoLoad;
	}

	/**
	 * ���ཨ������������֪ͨ��ݸı�
	 */
	public void notifyDataSetChanged() {
		BaseAdapter adapter = getMAdapter();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

    public void notifyDataSetInvalid() {
        BaseAdapter adapter = getMAdapter();
        if (adapter != null) {
            adapter.notifyDataSetInvalidated();
        }
    }

	// ��������
	protected BaseAdapter getMAdapter() {
		if (adapterView != null) {
			Adapter adapter = adapterView.getAdapter();
			if (adapter instanceof HeaderViewListAdapter) {
				return ((BaseAdapter) ((HeaderViewListAdapter) adapter).getWrappedAdapter());
			} else {
				return ((BaseAdapter) adapter);
			}
		} else {
			return null;
		}
	}

	/**
	 * �Ƿ��Ѿ���������<br>
	 * �������ʵ����header ���Ǳ���������ء�<br>
	 * ���ฺ��header��������Ϊ��
	 * 
	 * @return
	 */
	protected boolean isHaveItem() {
		return (mTaskMark != null && getMAdapter().getCount() > 0);
	}

	/**
	 * ������ͼ
	 * 
	 * @param parent
	 * @param context
	 * @return
	 */
	protected View createTopFrame(Context context) {
		return null;
	}

	/**
	 * �Ͳ���ͼ
	 * 
	 * @param parent
	 * @param context
	 * @return
	 */
	protected View createBottomFrame(Context context) {
		return null;
	}

	// �ײ�������ʾ��
	protected StatusFrame createStatusFrame(Context context) {
		StatusFrame statusView = new StatusFrame(context);
		statusView.setFocusable(false);
		statusView.hideNoteView();
		return statusView;
	}

	/**
	 * ������ͼ
	 * 
	 * @return
	 */
	protected abstract AdapterView createAdapterView();

	/**
	 * 
	 * @param mTaskMark
	 *            ���ع�����������
	 */
	protected abstract BaseAdapter createItemAdapter();

	/**
	 * ���Ի���µ���
	 */
	protected abstract void tryQueryNewItems();

	/**
	 * �Ƿ���Ҫת��item����¼�
	 * 
	 * @param item
	 * @return
	 */
	protected boolean isNeedDispatchItemClick(Object item) {
		return true;
	}

	/**
	 * ����ĳһʵ��������
	 */
	protected void handleRealItemClick(Object item) {

	}

	/**
	 * ���?��
	 * 
	 * @param item
	 */
	protected void handleRealItemLongClick(Object item) {

	}

	/**
	 * @return the taskMark
	 */
	public ATaskMark getTaskMark() {
		return mTaskMark;
	}

	// �����µ���
	protected void handleManualLoadItem() {
		mTaskMark.setTaskStatus(ATaskMark.HANDLE_OVER);
		tryQueryNewItems();
	}

	// �Ƿ�����ֶ�����
	protected boolean isCanManualLoad() {
		return (mTaskMark.getTaskStatus() == ATaskMark.HANDLE_ERROR || mTaskMark.getTaskStatus() == ATaskMark.HANDLE_WAIT);
	}

	// ״̬��ʾĬ��С���
	protected class StatusFrame extends FrameLayout implements OnClickListener {
		public static final int LOAD_LIST_ID = 0x02222;
		public static final int LOAD_NOTE_ID = 0x03333;

		protected View loadView;
		protected Button waitView;

		/**
		 * @param context
		 */
		public StatusFrame(Context context) {
			super(context);
			initNoteView(context);
		}

		/**
		 * @param context
		 * @param attrs
		 * @param defStyle
		 */
		public StatusFrame(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			initNoteView(context);
		}

		private void initNoteView(Context context) {
			setFocusable(false);

			loadView = LayoutInflater.from(context).inflate(R.layout.loading_view, null);
			loadView.setId(LOAD_LIST_ID);
			loadView.setFocusable(false);
			loadView.setVisibility(View.INVISIBLE);

			waitView = createWaitView();
			waitView.setId(LOAD_NOTE_ID);
			waitView.setFocusable(false);
			waitView.setOnClickListener(this);
			waitView.setVisibility(View.INVISIBLE);

			addNoteFrameView(context, loadView, waitView);
		}

		/**
		 * �ȴ���ͼ
		 */
		protected Button createWaitView() {
			return new Button(getContext());
		}

		/**
		 * �����ʾ���
		 * 
		 * @param loadView
		 * @param waitView
		 */
		protected void addNoteFrameView(Context context, View loadView, TextView waitView) {
			addView(loadView);

			waitView.setTextColor(getResources().getColor(R.color.load_note_fail));
			waitView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(
					R.dimen.wait_note_font_szie));
			waitView.setGravity(Gravity.CENTER);
			waitView.setBackgroundResource(R.drawable.reload_note);
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
					getResources().getDimensionPixelSize(R.dimen.wait_band_height));
			waitView.setLayoutParams(layoutParams);
			addView(waitView);
		}

		@Override
		public void onClick(View v) {
			if (v.getId() == LOAD_NOTE_ID && isCanManualLoad()) {
				handleManualLoadItem();
			}
		}

		public void showLoadView() {
			setVisibility(VISIBLE);
			waitView.setVisibility(GONE);
			loadView.setVisibility(VISIBLE);
		}

		public void showErrorView() {
			setVisibility(VISIBLE);
			waitView.setVisibility(VISIBLE);
			waitView.setText(R.string.load_error_retry);
			loadView.setVisibility(GONE);
		}

		public void showWaitView() {
			setVisibility(VISIBLE);
			waitView.setVisibility(VISIBLE);
			waitView.setText(R.string.load_wait);
			loadView.setVisibility(GONE);
		}

		public void hideNoteView() {
			setVisibility(GONE);
		}
	}

	/**
	 * ��ͼ״̬��ö��
	 */
	public enum ViewStatus {
		OVER, DOING, WAIT, ERROR;
	}

}

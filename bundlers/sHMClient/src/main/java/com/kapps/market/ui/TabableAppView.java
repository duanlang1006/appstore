package com.kapps.market.ui;

import java.util.LinkedHashMap;

import com.kapps.market.R;
import com.kapps.market.util.Constants;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;


/**
 * 2010-6-8 Ӧ�õĳ�����壬 �������TabActivity����ͼ���?�ܡ� ��ͬ��contentFrame�ṹ
 * 
 * @author admin
 * 
 */
public abstract class TabableAppView extends CommonView implements OnClickListener {

	public static final String TAG = "TabableAppView";
	// ��ͼ����,ϣ���������
	// key: ����ѡ�����ͼid, value:��ʾ��view
	// �û������˰�ť��ʱ�������ѡ�����ͼ����˳���л�
	private LinkedHashMap<Integer, View> viewCache = new LinkedHashMap<Integer, View>(3, 0.75f, false);
	// ��ʼ�����ҳ
	private FrameLayout contentFrame;
	private int rotateIndex;
	private int currentMark = 0;//Constants.NONE_VIEW;


	/**
	 * @param context
	 */
	public TabableAppView(Context context) {
		super(context);
	}

	@Override
	public void addView(int layoutResID) {
		// ���ݲ���
		super.addView(layoutResID);
		// �������
		contentFrame = (FrameLayout) findViewById(R.id.contentFrame);
	}

	@Override
	public void addView(View view) {
		// �������
		super.addView(view);
		contentFrame = (FrameLayout) findViewById(R.id.contentFrame);

	}

	@Override
	public void addView(View view, ViewGroup.LayoutParams params) {
		// �������
		super.addView(view, params);
		contentFrame = (FrameLayout) findViewById(R.id.contentFrame);

	}

	/**
	 * ע��ӵ�д�����ͼ��ʾ��viewͬʱ��� �����¼������?
	 * 
	 * @param trigger
	 */
	public final void registerTrigger(View trigger) {
		trigger.setOnClickListener(this);
	}

	/**
	 * ע����ͼ����
	 * 
	 * @param trigger
	 */
	public final void registerView(int viewMark) {
		viewCache.put(viewMark, null);
	}

	/**
	 * ȡ��һ����ͼ�����Ǻ����ʵ����˵�������Ժ��ٴ���ӵġ�
	 */

	public final void unregisterView(int viewMark) {
		viewCache.remove(viewMark);
	}

	/**
	 * ��ʼ����Ӧ����ͼ, �����������ʱ�ĳ�ʼ���� ����view��hashcode��Ϊ��ʶ��
	 * 
	 * @param triggerView
	 *            ˭������Ҫ��ʾ�����ͼ
	 */
	protected void showChoosedView(int viewMark) {
		showChoosedView(null, viewMark, null);
	}

	/**
	 * ��ʼ����Ӧ����ͼ, �����������ʱ�ĳ�ʼ���� ����view��hashcode��Ϊ��ʶ��
	 * 
	 * @param triggerView
	 *            ˭������Ҫ��ʾ�����ͼ
	 * @param data
	 *            ������һЩ���
	 */
	protected void showChoosedView(int viewMark, Object data) {
		showChoosedView(null, viewMark, data);
	}

	protected void preCreateView()
	{
		int count=viewCache.size();
		for(int i=0;i<count;i++)
		{
			View cacheView = createContentView(i);
			if (viewCache.containsKey(i)) {
				cacheView(i, cacheView);
			}
		}
	}
	/**
	 * ��ʼ����Ӧ����ͼ, �����������ʱ�ĳ�ʼ���� ����view��hashcode��Ϊ��ʶ��
	 * 
	 * @param triggerView
	 *            ˭������Ҫ��ʾ�����ͼ
	 * @param viewMark
	 *            �Ǹ���ͼ
	 * @param data
	 *            ������һЩ���
	 */
	protected void showChoosedView(View trigger, int viewMark, Object data) {
		boolean firstCreate = false;
//		preCreateView();
		View cacheView = viewCache.get(viewMark);
		if (cacheView == null) {
			firstCreate = true;
			cacheView = createContentView(viewMark);
			// ���֮ǰ��û��ע����򲻻���
			if (viewCache.containsKey(viewMark)) {
				cacheView(viewMark, cacheView);
			}
		}

		// ǰ
		onBeforeShowView(viewMark, data);

		// ȷ���Ƿ��Ѿ�����ʾ��Ҫ��ҳ����
		View currentView = getCurrentTab();
		if (currentView == null || currentView != cacheView) {
			if (currentView instanceof CommonView) {
				((CommonView) currentView).releaseView();
			}

			currentMark = viewMark;
	
			contentFrame.removeAllViews();
			contentFrame.addView(cacheView);

			// ��Ӧÿ�ζ��������ߵ�һ����ʱ�ǲ���Ҫ����flush�ġ�
			if (!firstCreate && (cacheView instanceof CommonView)) {
				((CommonView) cacheView).flushView(Constants.NONE_VIEW);
			}
		} else {
			onAlreadyShowView(viewMark, data);
			Log.d(TAG, "already choose View");
		}

		// ��
		onAfterShowView(viewMark, data);
		
	}
	
	/**
	 * ����ض���ʵ��զ����Ҫ����״̬�����ȿ����ڴ���ĵ�ʱ��<br>
	 * ������Ը��Ǵ˷�������ʵ�֣�����ÿ�ζ��������µ���ͼ���Ƴ����ͼ�� ���ա�
	 * 
	 * @param viewMark
	 * @param view
	 */
	protected void cacheView(int viewMark, View view) {
		viewCache.put(viewMark, view);
	}

	/**
	 * ����ѡ����ͼ��һ�������û����»��˰�ťʱ�����Ϊ��
	 * 
	 * @return ��ǰѡ�����ͼ
	 */
	@Override
	public boolean rotateContentView() {
		rotateIndex++;
		int nextIndex = rotateIndex % viewCache.size();
		Integer viewMark = (Integer) viewCache.keySet().toArray()[nextIndex];
		showChoosedView(viewMark);
		return true;
	}

	/*
	 * (non-Javadoc) ������¼�����ֻ�����ͼ��ѡ��������� ���ǣ��������ȵ��ø���ķ�����
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View trigger) {
		// ��
		int viewMark = getShowViewMark(trigger);
		// ȷ����ͼȷʵ����
		if (viewMark != Constants.NONE_VIEW) {
			showChoosedView(trigger, viewMark, null);
//			viewPager.setCurrentItem(viewMark); 
		}
	}
	protected void onSetButton(int viewMark, boolean b) {

	}
	/**
	 * ��ʾָ����ͼǰ������ʲô
	 */
	protected void onBeforeShowView(int viewMark, Object data) {

	}

	/**
	 * �����Ѿ���ʾ�������ͼʱʹ��
	 * 
	 * @param viewMark
	 * @param data
	 */
	protected void onAlreadyShowView(int viewMark, Object data) {

	}

	/**
	 * ��ʾָ����ͼ������ʲô
	 */
	protected void onAfterShowView(int viewMark, Object data) {

	}

	/**
	 * ˢ�µ�ǰҳ
	 */
	public void flushCurrentTabView(int what) {
		View view = getCurrentTab();
		if (view instanceof CommonView) {
			((CommonView) view).flushView(what);
		}
	}

	/**
	 * ��õ�ǰ��ͼ��־
	 * 
	 * @return
	 */
	public int getCurrentTabMark() {
		return currentMark;
	}

	/**
	 * ��õ�ǰ��ʾ����ͼ
	 * 
	 * @return
	 */
	public View getCurrentTab() {
		if (contentFrame.getChildCount() > 0) {
			return viewCache.get(currentMark);
		} else {
			return null;
		}
	}

	/*
	 * ���֪����ʾ����ͼ ���û�л�����ô����null
	 */
	protected View getTabByMarkFromCache(int viewMark) {
		View listView = viewCache.get(viewMark);
		return listView;
	}

	/**
	 * ��Ҫ��ˢ����ͼ�����CommonAppView��ͼ��
	 * 
	 * @param viewMark
	 *            ��ͼ��־
	 */
	public void flushView(int viewMark, int what) {
		View view = getTabByMarkFromCache(viewMark);
		if (view instanceof CommonView) {
			((CommonView) view).flushView(what);

		} else if (view instanceof TabableAppView) {
			((TabableAppView) view).flushView(what);
		}
	}

	@Override
	public void flushView(int what) {
		flushCurrentTabView(what);
	}

	/**
	 * ��ô�������Ҫ����ͼ
	 * 
	 * @param trigger
	 * @return
	 */
	protected abstract int getShowViewMark(View trigger);

	/**
	 * �ɾ������ഴ������������ͼ
	 * 
	 * @return
	 */
	protected abstract View createContentView(int viewMark);

}

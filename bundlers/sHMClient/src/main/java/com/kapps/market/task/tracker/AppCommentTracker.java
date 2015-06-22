package com.kapps.market.task.tracker;

import com.kapps.market.bean.AppComment;
import com.kapps.market.bean.PageableResult;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.CommentsTaskMark;

import java.util.List;


/**
 * 2010-6-26<br>
 * Ӧ�õ�ע�͸�����
 * 
 * @author admin
 * 
 */
public class AppCommentTracker extends AInvokeTracker {

	private static final String TAG = "AppCommentsTracker";

	public AppCommentTracker(IResultReceiver iReceiver) {
		super(iReceiver);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleResult(OperateResult result) {
		CommentsTaskMark taskWraper = (CommentsTaskMark) result.getTaskMark();
		// ���óɹ�
		PageableResult pageableResult = (PageableResult) result.getResultData();
		if (pageableResult.getContent() != null) {
			// ��ӵ�����, ����ʹ�û���Ľӿڵ�Ŀ¼�Ǳ�֤������۵�comment���ǵ�ʱ����
			// ���۵�comment��
			appCahceManager.addAppCommentToCache(taskWraper, (List<AppComment>) pageableResult.getContent());
		}

		// ����ҳ����Ϣ
		taskWraper.setPageInfo(pageableResult.getPageInfo());
		LogUtil.d(TAG, "handleResult pageableResult: " + pageableResult + " taskWraper: " + taskWraper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.task.tracker.AInvokeTracker#TAG()
	 */
	@Override
	public String TAG() {
		// TODO Auto-generated method stub
		return TAG;
	}

}

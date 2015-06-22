package com.kapps.market.task.tracker;

import com.kapps.market.bean.AppComment;
import com.kapps.market.bean.CommentMark;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;

/**
 * 2010-6-27<br>
 * ���۱��
 * 
 * @author admin
 * 
 */
public class CommitCommentMarkTracker extends AInvokeTracker {

	public static final String TAG = "CommitCommentMarkTracker";

	/**
	 * @param resultReceiver
	 */
	public CommitCommentMarkTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
		// TODO Auto-generated constructor stub
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ck.market.task.tracker.AInvokeTracker#handleResult(com.ck.market
	 * .task.OperateResult)
	 */
	@Override
	public void handleResult(OperateResult result) {
		// ��ǰ����ǵ�����
		AppComment appComment = (AppComment) result.getAttach();
		// �滻�µı��
		appComment.setOldCommentMark(appComment.getCommentMark());
		appComment.setCommentMark(AppComment.UN_MARK);

		CommentMark commentMark = (CommentMark) result.getResultData();
		if (commentMark != null) {
			appComment.setButt(commentMark.getButt());
			appComment.setStamp(commentMark.getStamp());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ck.market.task.tracker.AInvokeTracker#handleFault(com.ck.market
	 * .task.OperateResult)
	 */
	@Override
	public void handleFault(OperateResult result) {
		AppComment appComment = (AppComment) result.getAttach();
		appComment.setCommentMark(AppComment.UN_MARK);
	}

}

package com.kapps.market.ui.detail;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kapps.market.R;
import com.kapps.market.bean.AppComment;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.PageInfo;
import com.kapps.market.log.LogUtil;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.CommentsTaskMark;
import com.kapps.market.task.mark.CommitCommentMarkTaskMark;
import com.kapps.market.task.mark.CommitCommentTaskMark;
import com.kapps.market.ui.ListItemsBroswer;
import com.kapps.market.ui.RatingView;
import com.kapps.market.util.Util;

/**
 * @author admin Ӧ�õ�����
 */
public class AppCommentView extends ListItemsBroswer implements OnClickListener, IResultReceiver {

	public static final String TAG = "AppCommentView";
	// ������Ӧ��
	private AppItem appItem;
	private View myCommentView;

	/**
	 * @param context
	 */
	public AppCommentView(Context context, AppItem appItem) {
		super(context);
		this.appItem = appItem;

	}

	/**
	 * ����Ĭ�ϵ�ListView�ķ��Ͳ������಻Ҫ��������ʵ��
	 */
	@Override
	protected void setListViewParameter(ListView listView) {
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setDivider(getResources().getDrawable(R.drawable.line));
		listView.setFadingEdgeLength(0);
	}

	@Override
	protected void addListHeader(ListView listView) {
		myCommentView = LayoutInflater.from(getContext()).inflate(R.layout.my_comment, null);
		// �ҵ�����(��dialog��ʾ)
		myCommentView.findViewById(R.id.myCommentView).setOnClickListener(this);
		initMyComment(appItem.getMyComment());

		listView.addHeaderView(myCommentView);
	}

	// �ҵ�����
	private void initMyComment(AppComment appComment) {
		if (appComment != null) {
			TextView myCommentContent = (TextView) myCommentView.findViewById(R.id.commentContentField);
			myCommentContent.setText(appComment.getContent());
			RatingView ratingView = (RatingView) myCommentView.findViewById(R.id.commentRatingView);
			ratingView.setRating(Util.getDrawRateVaue(appComment.getRating()));
		}
	}

	// ��ʾ�ҵ�����
	private void showMyCommentDialog() {
		final View commentView = LayoutInflater.from(getContext()).inflate(R.layout.my_app_comment, null);
		// ��ʼ���ҵ�������ͼ
		final RatingBar ratingBar = (RatingBar) commentView.findViewById(R.id.commentRatingBar);
		final TextView contentText = (TextView) commentView.findViewById(R.id.commentContentField);
		final TextView errorLabel = (TextView) commentView.findViewById(R.id.errorLabel);
		final Dialog dialog = new AlertDialog.Builder(getContext()).setIcon(R.drawable.comment)
				.setTitle(getResources().getString(R.string.my_comment)).setView(commentView).create();
		commentView.findViewById(R.id.commitCommentButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				float rating = ratingBar.getRating();
				String content = contentText.getText().toString();
				if (rating <= 0) {
					errorLabel.setText(getResources().getString(R.string.need_score_software));

				} else {
					handleCommitMyComment(rating, content);
					dialog.dismiss();
				}
			}
		});
		commentView.findViewById(R.id.cancelCommentButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		AppComment appComment = appItem.getMyComment();
		if (appComment != null) {
			ratingBar.setRating(appComment.getRating());
			contentText.setText(appComment.getContent());

		} else {
			ratingBar.setRating(0);
			contentText.setText("");
		}

		dialog.show();
	}

	// �ύ����
	private void handleCommitMyComment(float rating, String content) {
		AppComment appComment = new AppComment();
		appComment.setAppId(appItem.getId());
		appComment.setAuthor(marketContext.getSharedPrefManager().getUserInfo().getName());
		appComment.setTime(Util.dateFormat(System.currentTimeMillis()));
		// �ύ�����������0-5�ĸ���
		appComment.setRating(rating);
		appComment.setContent(content);
		serviceWraper.commitAppComment(this, taskMarkPool.createCommitCommentTaskWraper(appItem.getId()), appComment,
				appComment, appItem.getPackageName());

	}

	@Override
	protected BaseAdapter createItemAdapter() {
		AppCommentApdapter appCommnetAdapter = new AppCommentApdapter();
		return appCommnetAdapter;
	}

	@Override
	protected void handleLoadNewItems(ATaskMark taskMark) {
		CommentsTaskMark commentsTaskMark = (CommentsTaskMark) taskMark;
		PageInfo pageInfo = commentsTaskMark.getPageInfo();
		// ���۵Ļ�ȡ��ַ
		serviceWraper.getAppCommentList(this, commentsTaskMark, appItem.getId(), appItem.getPackageName(),
				pageInfo.getNextPageIndex(), pageInfo.getPageSize());
	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		super.receiveResult(taskMark, exception, trackerResult);

		if (taskMark instanceof CommitCommentTaskMark) {
			if (taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
				initMyComment(appItem.getMyComment());
				Toast.makeText(getContext(), getResources().getString(R.string.commit_comment_success), 200).show();

			} else {
				Toast.makeText(getContext(),
						getResources().getString(R.string.commit_comment_fail) + " " + exception.getExMessage(), 200)
						.show();
			}

		} else if (taskMark instanceof CommitCommentMarkTaskMark && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
			notifyDataSetChanged();
			Toast.makeText(getContext(), getResources().getString(R.string.commit_comment_mark_success), 100).show();
		}

	}

	// ����������
	private class AppCommentApdapter extends BaseAdapter {

		public AppCommentApdapter() {
			super();
		}

		// �������ʹ���¼������б��еĳ���
		@Override
		public int getCount() {
			return appItem.getCommentList().size();
		}

		@Override
		public AppComment getItem(int position) {
			// TODO Auto-generated method stub
			return appItem.getCommentList().get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		/*
		 * (non-Javadoc) ����ֻ��app count ��Ϊ0��ʱ��Żᱻ���á�
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// ����Ƿ�Ҫ�����µ���
			if (position == (getCount() - 1)) {
				tryQueryNewItems();
			}
			// ��ǰλ����ͼ
			if (convertView == null) {
				convertView = createItemView(parent);
			}
			// ��ó�ʼ���õ���ͼ
			AppComment appComment = getItem(position);
			if (appComment != null) {
				initCommentItemView(convertView, appComment);

			} else {
				LogUtil.w(TAG, "can not find appComment: position = " + position + " taskMark = " + mTaskMark);

			}
			return convertView;
		}

		protected View createItemView(ViewGroup parent) {
			View convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_item, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			TextView textView = (TextView) convertView.findViewById(R.id.authorLabel);
			viewHolder.authorLabel = textView;
			textView = (TextView) convertView.findViewById(R.id.dateLabel);
			viewHolder.dateLabel = textView;
			textView = (TextView) convertView.findViewById(R.id.contentLabel);
			viewHolder.contentLabel = textView;
			RatingView ratingView = (RatingView) convertView.findViewById(R.id.commentRatingBar);
			viewHolder.ratingView = ratingView;

			convertView.setTag(viewHolder);

			return convertView;
		}

		// ��ʼ������ͼ
		protected void initCommentItemView(View convertView, AppComment appComment) {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.authorLabel
					.setText((appComment.getAuthor() == null || appComment.getAuthor().length() == 0) ? getResources()
							.getString(R.string.none_user) : appComment.getAuthor());

			viewHolder.dateLabel.setText(appComment.getTime());
			viewHolder.contentLabel.setText(appComment.getContent());
			viewHolder.ratingView.setRating((int) appComment.getRating());
		}

		// ��ͼ���
		private class ViewHolder {
			RatingView ratingView;
			TextView authorLabel;
			TextView contentLabel;
			TextView dateLabel;
		}
	}

	@Override
	protected boolean isNeedDispatchItemClick(Object item) {
		return (item instanceof AppComment);
	}

	@Override
	public void onClick(View arg0) {
		// ��������Ƿ��Ѿ�����
		if (serviceWraper.isTaskExist(marketContext.getTaskMarkPool().createCommitCommentTaskWraper(appItem.getId()))) {
			Toast.makeText(getContext(), getResources().getString(R.string.wait_for_commenting), 150).show();
		} else {
			showMyCommentDialog();
		}

	}

}

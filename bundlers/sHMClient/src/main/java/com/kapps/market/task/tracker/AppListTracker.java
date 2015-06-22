package com.kapps.market.task.tracker;

import android.util.Log;

import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.PageableResult;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.OperateResult;
import com.kapps.market.task.mark.APageTaskMark;
import com.kapps.market.task.mark.AppAdvertiseTaskMark;
import com.kapps.market.task.mark.AuthorAppTaskMark;
import com.kapps.market.util.ResourceEnum;

import java.util.List;



/**
 * 2010-6-26<br>
 * Ӧ���������ṹ��������
 * 
 * @author admin
 * 
 */
public class AppListTracker extends AInvokeTracker {

	private static final String TAG = "AppItemsTracker";

	public AppListTracker(IResultReceiver iReceiver) {
		super(iReceiver);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleResult(OperateResult result) {
		// TODO Auto-generated method stub
		APageTaskMark taskWraper = (APageTaskMark) result.getTaskMark();
		// ���óɹ�
		PageableResult pageableResult = (PageableResult) result.getResultData();
		if (pageableResult.getContent() != null) {
			List<AppItem> appItemList = (List<AppItem>) (pageableResult.getContent());
			// �ǹ�������Ҫ���¾ɵ������Ϣ
			if (taskWraper instanceof AppAdvertiseTaskMark) {
				AppItem oldItem = null;
				for (AppItem appItem : appItemList) {
					oldItem = appCahceManager.getAppItemById(appItem.getId());
					if (oldItem != null) {
						oldItem.setAdDes(appItem.getAdDes());
						oldItem.setAdIcon(appItem.getAdIcon());
                        oldItem.setCategoryId(appItem.getCategoryId());
					}
				}
			}

			// ��ӵ�����
			appCahceManager.addAppItemToCache(taskWraper, appItemList);
		}

		// ����ҳ����Ϣ(��д���͵��б�û��ҳ��ͳ����Ϣ)
		// ���ص�pageinfoΪnull��ô��ζ���������ֻ��Ҫ����б�һ��
		// �鿴 APageTaskMark��isLoadEndʵ��
		// ����ǹ��������ö����������ô����һ�ξͲ��ڼ����ˣ����ܿ��ǣ�
		if (taskWraper instanceof AppAdvertiseTaskMark
				&& ((AppAdvertiseTaskMark) taskWraper).getPopType() == ResourceEnum.AD_TYPE_TOP) {
			taskWraper.setPageInfo(null);
			if (LogUtil.opDebug) {
				Log.d(TAG, "handleResult AD_TYPE_TOP set pageinfo to null");
			}

		}else if(taskWraper instanceof AuthorAppTaskMark){
			taskWraper.setPageInfo(null);
			if (LogUtil.opDebug) {
				Log.d(TAG, "handleResult AuthorAppTaskMark set pageinfo to null");
			}
			
		} else {
			taskWraper.setPageInfo(pageableResult.getPageInfo());
			
		}

		LogUtil.d(TAG, "handleResult pageableResult: " + pageableResult + " taskWraper: " + taskWraper);
	}

	@Override
	public String TAG() {
		// TODO Auto-generated method stub
		return TAG;
	}

}

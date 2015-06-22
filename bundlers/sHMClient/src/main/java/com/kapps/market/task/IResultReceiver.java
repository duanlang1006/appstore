package com.kapps.market.task;

import com.kapps.market.service.ActionException;
import com.kapps.market.task.mark.ATaskMark;

/**
 * ������÷�Χ����ͼ�㣬���ִ�н����к������ʵ��<br>
 * �����tracker�Ѿ����?������ˣ� ��ͼ������ȫ���ɻ���ģ�����ͳһ���?
 * 
 * @author admin
 * 
 */
public interface IResultReceiver {

	/**
	 * ������ִ����ϵ�ʱ��Ļص��ӿ� ע�⣬������������¼��߳��е��á�
	 * 
	 * @param taskMark
	 *            ������
	 * @param exception
	 *            ���ֻ�ڴ����з�����󣬰���ʵ�ʷ����׳��쳣��ʱ�����ֵ������Ϊnull,<br>
	 *            ���������ж�ATaskMark.HANDLE_ERROR
	 * @param trackerResult
	 *            �����е�һ�����ٽ��
	 */
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult);

}

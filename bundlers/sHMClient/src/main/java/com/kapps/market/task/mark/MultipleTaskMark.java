package com.kapps.market.task.mark;

import java.util.ArrayList;
import java.util.List;

/**
 * 2010-7-24<br>
 * ����һ�����԰���������ĵ�������
 * 
 * @author admin
 * 
 */
public class MultipleTaskMark extends ATaskMark {

	// �������б�
	private List<ATaskMark> taskMarkList = new ArrayList<ATaskMark>();

	/**
	 * @param appId
	 * @param type
	 */
	public MultipleTaskMark() {
		super();
	}

	/**
	 * ���������
	 * 
	 * @param taskMark
	 */
	public void addSubTaskMark(ATaskMark taskMark) {
		taskMarkList.add(taskMark);
	}

	/**
	 * @return the taskMarkList
	 */
	public List<ATaskMark> getTaskMarkList() {
		return taskMarkList;
	}

	/**
	 * ��������Ϊ��Ч
	 */
	public void invalidTaskMark() {
		taskMarkList.clear();
	}

	/**
	 * �����һ������
	 * 
	 * @param ������һ����Ҫִ�е�����
	 *            ������null��ʾ�������
	 */
	public ATaskMark pickNextTaskMark() {
		if (taskMarkList.size() > 0) {
			return taskMarkList.remove(0);
		} else {
			return null;
		}
	}

}

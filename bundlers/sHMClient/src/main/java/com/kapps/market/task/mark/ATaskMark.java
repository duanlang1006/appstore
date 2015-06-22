package com.kapps.market.task.mark;


/**
 * 2010-6-22 <br>
 * A task mark has some task status
 * 
 * @author shuizhu
 * 
 */
public abstract class ATaskMark {

	// û�м��ػ����ϴ��Ѿ��ɹ����ؽ���
	// ��Ӧhttp ok: 200
	public static final int HANDLE_OVER = 0;
	// ������
	public static final int HANDLE_DOING = 1;
	// �������
	// ����������ʱ�����Ĵ�����Ϣ��װ�ڶ�Ӧ��ServiceException�С�
	public static final int HANDLE_ERROR = 2;
	// �ȴ����
	public static final int HANDLE_WAIT = 3;
	// ��ʼΪû��ֵ����ؽ���
	protected int taskStatus = HANDLE_WAIT;

	// ���������Ψһ������
	public static final int UNIQUE = -494949;

	/**
	 * @return the loadStatus
	 */
	public int getTaskStatus() {
		return taskStatus;
	}

	/**
	 * @param taskStatus
	 *            the loadStatus to set
	 */
	public void setTaskStatus(int taskStatus) {
		this.taskStatus = taskStatus;
	}

	/**
	 * @return the loadEnd
	 */
	public boolean isLoadEnd() {
		return taskStatus == HANDLE_OVER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ATaskMark [taskStatus=" + taskStatus + "]";
	}

}

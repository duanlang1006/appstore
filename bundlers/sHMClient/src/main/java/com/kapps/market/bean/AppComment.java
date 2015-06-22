package com.kapps.market.bean;

/**
 * 2010-6-9
 * 
 * @author admin
 * 
 */
public class AppComment {
	private int id;
	// Ӧ��id�����ĸ�Ӧ�õ�����
	private int appId;
	// ����
	private String author;
	// ʱ��
	private String time;
	// ����
	private String content;
	// ��ֵ (����ʱ����˼�¼�Ǽ�)
	private float rating;
	// ��
	private int butt;
	// ��
	private int stamp;
	public static final int UN_MARK = -1;
	private int oldCommentMark = UN_MARK;
	// �ҵ����۱�� -1 ��Ч
	private int commentMark = UN_MARK;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author
	 *            the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the rating
	 */
	public float getRating() {
		return rating;
	}

	/**
	 * @param rating
	 *            the rating to set
	 */
	public void setRating(float rating) {
		this.rating = rating;
	}

	/**
	 * @return the butt
	 */
	public int getButt() {
		return butt;
	}

	/**
	 * @param butt
	 *            the butt to set
	 */
	public void setButt(int butt) {
		this.butt = butt;
	}

	/**
	 * @return the stamp
	 */
	public int getStamp() {
		return stamp;
	}

	/**
	 * @param stamp
	 *            the stamp to set
	 */
	public void setStamp(int stamp) {
		this.stamp = stamp;
	}

	/**
	 * @return the commentMark
	 */
	public int getCommentMark() {
		return commentMark;
	}

	/**
	 * @param commentMark
	 *            the commentMark to set
	 */
	public void setCommentMark(int commentMark) {
		this.commentMark = commentMark;
	}

	/**
	 * @return the commentOldMark
	 */
	public int getOldCommentMark() {
		return oldCommentMark;
	}

	/**
	 * @param oldCommentMark
	 *            the oldCommentMark to set
	 */
	public void setOldCommentMark(int oldCommentMark) {
		this.oldCommentMark = oldCommentMark;
	}

	/**
	 * @return the appId
	 */
	public int getAppId() {
		return appId;
	}

	/**
	 * @param appId
	 *            the appId to set
	 */
	public void setAppId(int appId) {
		this.appId = appId;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

}

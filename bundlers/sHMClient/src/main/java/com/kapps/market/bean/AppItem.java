package com.kapps.market.bean;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 2010-6-8 Ӧ
 * 
 * @author admin
 * 
 */
public class AppItem extends BaseApp {

	private static final long serialVersionUID = 20100712L;
	// ��ֵ
	private int rating;
	// �����б?��ʱ����
	private transient List<AppComment> commentList;
	// Ȩ���б?��ʱ����
	private transient List<AppPermission> permissionList;
	// �ҵ�����
	private transient AppComment myComment;
	// �Դ�Ӧ�õĲ�������
	private transient AppBadness appBadness;
	// ������
	private String authorName;
	// ���ͼƬ
	private String adIcon;
	// �������
	private String adDes;
	// �������
	private int adType;

	// ------------------ ��ϸ������� ----------------------//
	private AppDetail appDetail;
    private int categoryId;

    public AppItem() {
	}

	@Override
	public int getIconType() {
		return MImageType.APP_ICON;
	}

	/**
	 * @return the rating
	 */
	public int getRating() {
		return rating;
	}

	/**
	 * @param rating
	 *            the rating to set
	 */
	public void setRating(int rating) {
		this.rating = rating;
	}

	/**
	 * @return the myComment
	 */
	public AppComment getMyComment() {
		return myComment;
	}

	/**
	 * @param myComment
	 *            the myComment to set
	 */
	public void setMyComment(AppComment myComment) {
		this.myComment = myComment;
	}

	/**
	 * @return the commentList
	 */
	public List<AppComment> getCommentList() {
		if (commentList == null) {
			commentList = new ArrayList<AppComment>();
		}
		return commentList;
	}

	/**
	 * @return the permistionList
	 */
	public List<AppPermission> getPermissionList() {
		if (permissionList == null) {
			permissionList = new ArrayList<AppPermission>();
		}
		return permissionList;
	}

	/**
	 * @param permissionList
	 *            the permistionList to set
	 */
	public void setPermissionList(List<AppPermission> permissionList) {
		this.permissionList = permissionList;
	}

	/**
	 * @return the appBadness
	 */
	public AppBadness getAppBadness() {
		return appBadness;
	}

	/**
	 * @param appBadness
	 *            the appBadness to set
	 */
	public void setAppBadness(AppBadness appBadness) {
		this.appBadness = appBadness;
	}

	/**
	 * @return the autorName
	 */
	public String getAuthorName() {
		return authorName;
	}

	/**
	 * @param authorName
	 *            the autorName to set
	 */
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	/**
	 * @return the adIcon
	 */
	public String getAdIcon() {
		return adIcon;
	}

	/**
	 * @param adIcon
	 *            the adIcon to set
	 */
	public void setAdIcon(String adIcon) {
		this.adIcon = adIcon;
	}

	/**
	 * @return the adDes
	 */
	public String getAdDes() {
		return adDes;
	}

	/**
	 * @param adDes
	 *            the adDes to set
	 */
	public void setAdDes(String adDes) {
		this.adDes = adDes;
	}

	/**
	 * @return the adType
	 */
	public int getAdType() {
		return adType;
	}

	/**
	 * @param adType
	 *            the adType to set
	 */
	public void setAdType(int adType) {
		this.adType = adType;
	}

	/**
	 * @return the appDetail
	 */
	public AppDetail getAppDetail() {
		return appDetail;
	}

	/**
	 * @param appDetail
	 *            the appDetail to set
	 */
	public void setAppDetail(AppDetail appDetail) {
		this.appDetail = appDetail;
	}

	/**
	 * @param commentList
	 *            the commentList to set
	 */
	public void setCommentList(List<AppComment> commentList) {
		this.commentList = commentList;
	}

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getCategoryId() {
        return categoryId;
    }
}

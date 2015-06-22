package com.applite.util;

import android.graphics.Bitmap;

public class AppDetails {
	private String detail;
	private Bitmap bitmap;
	public AppDetails() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AppDetails(String detail, Bitmap bitmap) {
		super();
		this.detail = detail;
		this.bitmap=bitmap;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	

}

package com.android.applite.view;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

public class GalleryFlow extends Gallery {
	/**
	 * The camera class is used to 3D transformation matrix.
	 */
	private Camera mCamera = new Camera();

	/**
	 * The max rotation angle.
	 */
	private int mMaxRotationAngle = 60;

	/**
	 * The max zoom value (Z axis).
	 */
	private int mMaxZoom = 0;

	/**
	 * The center of the gallery.
	 */
	private int mCoveflowCenter = 0;

	public GalleryFlow(Context context) {
		this(context, null);
	}

	public GalleryFlow(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GalleryFlow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		// 设置为true时，允许多子类进行静态转换,每次viewGroup在重新画它的child的时候都会促发getChildStaticTransformation
		this.setStaticTransformationsEnabled(true);
		// Enable set the children drawing order.
		this.setChildrenDrawingOrderEnabled(true);
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * 改变子类的绘制顺序,在这之前必须首先调用setChildrenDrawingOrderEnabled(boolean)来允许子类排序。
	 */
	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		// Current selected index.
		int selectedIndex = getSelectedItemPosition()
				- getFirstVisiblePosition();
		if (selectedIndex < 0) {
			return i;
		}

		if (i < selectedIndex) {
			return i;
		} else if (i >= selectedIndex) {
			return childCount - 1 - i + selectedIndex;
		} else {
			return i;
		}
	}
	/**
	 * 在布局发生变化时的回调函数，间接回去调用onMeasure, onLayout函数重新布局
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mCoveflowCenter = getCenterOfCoverflow();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private int getCenterOfView(View view) {
		return view.getLeft() + view.getWidth() / 2;
	}
	/**
	 * setStaticTransformationsEnabled这个属性设成true的时候每次viewGroup画它的child的时候都会促发getChildStaticTransformation这个函数
	 */
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		super.getChildStaticTransformation(child, t);

		final int childCenter = getCenterOfView(child);//获得View的中点
		final int childWidth = child.getWidth();

		int rotationAngle = 0;
		t.clear();//重新设置
		//Transformation.TYPE_MATRIX
		t.setTransformationType(Transformation.TYPE_BOTH);

		// If the child is in the center, we do not rotate it.
		if (childCenter == mCoveflowCenter) {
			transformImageBitmap(child, t, 0);
		} else {
			// Calculate the rotation angle.
			rotationAngle = (int) (((float) (mCoveflowCenter - childCenter) / childWidth) * mMaxRotationAngle);

			// Make the angle is not bigger than maximum.
			if (Math.abs(rotationAngle) > mMaxRotationAngle) {
				rotationAngle = (rotationAngle < 0) ? -mMaxRotationAngle : mMaxRotationAngle;
			}

			transformImageBitmap(child, t, rotationAngle);
		}

		return true;
	}

	private int getCenterOfCoverflow() {
		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2
				+ getPaddingLeft();
	}

	private void transformImageBitmap(View child, Transformation t,
			int rotationAngle) {
		mCamera.save();

		final Matrix imageMatrix = t.getMatrix();
		final int imageHeight = child.getHeight();
		final int imageWidth = child.getWidth();
		final int rotation = Math.abs(rotationAngle);

		// Zoom on Z axis.(在Z轴上正向移动camera的视角，实际效果为放大图片; 如果在Y轴上移动，则图片上下移动; X轴上对应图片左右移动)
		mCamera.translate(0, 0, mMaxZoom);

		if (rotation < mMaxRotationAngle) {
			float zoomAmount = (float) (mMaxZoom + rotation * 1.5f);
			mCamera.translate(0, 0, zoomAmount);
		}

        // 随着角度的减小而放大
        if (rotation < mMaxRotationAngle) {//注意mMaxZoom是负的
            float zoomAmount = (float) (mMaxZoom + (rotation * 1.5));
            mCamera.translate(0.0f, 0.0f, zoomAmount);
        } 
        // rotationAngle 为正，沿y轴向内旋转； 为负，沿y轴向外旋转   
        mCamera.rotateY(rotationAngle);         
        
        mCamera.getMatrix(imageMatrix);//把Camera的动作矩阵传到imageMatrix
        imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
        imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
        mCamera.restore();
	}
}

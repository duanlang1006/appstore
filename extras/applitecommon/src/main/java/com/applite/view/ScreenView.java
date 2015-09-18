package com.applite.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.applite.common.AppliteUtils;
import com.applite.common.R;

/**
 * Created by LSY on 15-9-17.
 */
public class ScreenView extends PopupWindow {

    private ImageView mPopImgView;
    private ImageView mPopExitView;
    private View mPopView;

    public ScreenView(Activity context, View.OnClickListener itemsOnClick) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPopView = inflater.inflate(R.layout.popupwindow_img, null);
        mPopImgView = (ImageView) mPopView.findViewById(R.id.pop_img_img);
        mPopExitView = (ImageView) mPopView.findViewById(R.id.pop_img_exit);
        //设置按钮监听
        mPopImgView.setOnClickListener(itemsOnClick);
        mPopExitView.setOnClickListener(itemsOnClick);
        //设置SelectPicPopupWindow的View
        this.setContentView(mPopView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
//        this.setAnimationStyle(R.style.AnimBottom);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mPopView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mPopView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
//                int top = mPopImgView.getTop();
//                int left = mPopImgView.getLeft();
//                int right = mPopImgView.getRight();
//                int bottom = mPopImgView.getBottom();
//                int y = (int) event.getY();
//                int x = (int) event.getX();
                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (y < top || y > bottom || x < left || x > right) {
                    dismiss();
//                    }
                }
                return true;
            }
        });
    }

    public void setImageBitmap(String path) {
        mPopImgView.setImageBitmap(AppliteUtils.getLoacalBitmap(path));
    }

    public void setImageBitmap(Bitmap bitmap) {
        mPopImgView.setImageBitmap(bitmap);
    }

}


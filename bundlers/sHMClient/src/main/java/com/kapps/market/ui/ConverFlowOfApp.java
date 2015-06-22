package com.kapps.market.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.kapps.market.util.ResourceEnum;

/**
 * 图片轮播
 * 
 * @author shuizhu
 * 
 */
public class ConverFlowOfApp extends ConverFlowBase {

	public static final String TAG = "ConverFlowOfApp";

    public ConverFlowOfApp(Context context, AttributeSet attrSet) {
        super(context, attrSet);
    }

    @Override
    protected void initAdType() {
        taskMark = taskMarkPool.getAppAdvertiseTaskMark(ResourceEnum.AD_TYPE_EXCEL);//TOP
    }
}

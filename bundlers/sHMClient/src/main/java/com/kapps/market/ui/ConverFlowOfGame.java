package com.kapps.market.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.kapps.market.util.ResourceEnum;

/**
 * 图片轮播
 * 
 * @author shuizhu
 * 
 */
public class ConverFlowOfGame extends ConverFlowBase {

	public static final String TAG = "ConverFlowOfGame";

    public ConverFlowOfGame(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        mType = CONVERFLOW_GAME;
    }

    @Override
    protected void initAdType() {
        Log.d("temp", "loadAdvtisement-->in.");
        taskMark = taskMarkPool.getAppAdvertiseTaskMark(ResourceEnum.AD_TYPE_EXCEL);
    }
}

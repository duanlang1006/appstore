package com.applite.homepage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.applite.utils.DataCleanManager;

/**
 * Created by caijian on 15-9-10.
 */

/**
 * 清除缓存Dialog
 */
public class DataCleanDialog {

    private CallBackInterface mCallback;

    public void CallBack(CallBackInterface mCallback){
        this.mCallback = mCallback;
    }

    public interface CallBackInterface {
        void refreshCacheSize();
    }

    public void show(final Context context) {
//        LayoutInflater inflater = LayoutInflater.from(context);
//        View layout = inflater.inflate(R.layout.dialog_dataclean, null);
//        new AlertDialog.Builder(context)
//                .setView(layout)
//                .setView(layout).setNegativeButton("取消", null)
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        DataCleanManager.cleanAllCache(context);
//                        Toast.makeText(context, "清除成功", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .show();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.warning)
                .setTitle(R.string.dialog_dataclean_title)
                .setMessage(R.string.dialog_dataclean_content)
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // do nothing
                            }
                        })
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DataCleanManager.cleanAllCache(context);
                                if(null != mCallback)
                                mCallback.refreshCacheSize();
                            }
                        })
                .show();
    }

}

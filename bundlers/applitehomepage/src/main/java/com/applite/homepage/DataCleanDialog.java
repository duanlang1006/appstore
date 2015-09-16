package com.applite.homepage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.applite.utils.DataCleanManager;

/**
 * Created by caijian on 15-9-10.
 */

/**
 * 清除缓存Dialog
 */
public class DataCleanDialog {
    public static void show(final Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.dialog_dataclean, null);
        new AlertDialog.Builder(context)
                .setView(layout)
                .setView(layout).setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataCleanManager.cleanAllCache(context);
                        Toast.makeText(context, "清除成功", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}

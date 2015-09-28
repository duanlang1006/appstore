package com.applite.homepage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.SyncListener;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;

import java.lang.reflect.Field;
import java.util.List;

//import com.applite.utils.ButtonHandler;


/**
 * Created by caijian on 15-9-10.
 */

/**
 * 意见反馈Dialog
 */
public class FeedbackDialog {
    public void show(final Context context) {
        final Conversation mComversation = new FeedbackAgent(context).getDefaultConversation();
        LayoutInflater inflater = LayoutInflater.from(context);
        final View layout = inflater.inflate(R.layout.dialog_feedback, null);
        final EditText et_feedback = (EditText) layout.findViewById(R.id.dialog_feedback_et1);
        final EditText et_contact = (EditText) layout.findViewById(R.id.dialog_feedback_et2);
        final CheckBox rb1 = (CheckBox) layout.findViewById(R.id.dialog_feedback_btn1);
        final CheckBox rb2 = (CheckBox) layout.findViewById(R.id.dialog_feedback_btn2);
        final CheckBox rb3 = (CheckBox) layout.findViewById(R.id.dialog_feedback_btn3);
        final CheckBox rb4 = (CheckBox) layout.findViewById(R.id.dialog_feedback_btn4);
        final CheckBox rb5 = (CheckBox) layout.findViewById(R.id.dialog_feedback_btn5);
        final CheckBox rb6 = (CheckBox) layout.findViewById(R.id.dialog_feedback_btn6);

        AlertDialog.Builder mDialog = new AlertDialog.Builder(context);
        mDialog.setView(layout)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setPositiveButton("提交", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String reason = "";
                        String feedback = et_feedback.getText().toString();
                        String contact = et_contact.getText().toString();
                        if (rb1.isChecked()) {
                            reason = reason + rb1.getTag().toString();
                        }
                        if (rb2.isChecked()) {
                            reason = reason + "," + rb2.getTag().toString();
                        }
                        if (rb3.isChecked()) {
                            reason = reason + "," + rb3.getTag().toString();
                        }
                        if (rb4.isChecked()) {
                            reason = reason + "," + rb4.getTag().toString();
                        }
                        if (rb5.isChecked()) {
                            reason = reason + "," + rb5.getTag().toString();
                        }
                        if (rb6.isChecked()) {
                            reason = reason + "," + rb6.getTag().toString();
                        }
                        if (!(rb1.isChecked()) && (rb2.isChecked()) && (rb3.isChecked()) && (rb4.isChecked()) && (rb5.isChecked()) && (rb6.isChecked())) {
                            reason = "";
                        }
                        if (TextUtils.isEmpty(reason) && feedback.isEmpty()) {
                            try {
                                Field field = dialog.getClass()
                                        .getSuperclass().getDeclaredField(
                                                "mShowing");
                                field.setAccessible(true);
                                //将mShowing变量设为false，表示对话框已关闭
                                field.set(dialog, false);
                                dialog.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(context, "您还未提出意见或建议", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            mComversation.addUserReply("反馈原因:" + reason + "\t意见建议:" + feedback + "\t联系方式:" + contact);
                            mComversation.sync(new SyncListener() {
                                @Override
                                public void onReceiveDevReply(List<Reply> list) {

                                }

                                @Override
                                public void onSendUserReply(List<Reply> list) {

                                }
                            });
                            Toast.makeText(context, "提交成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

}

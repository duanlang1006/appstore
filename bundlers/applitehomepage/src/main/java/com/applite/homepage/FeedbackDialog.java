package com.applite.homepage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

//import com.applite.utils.ButtonHandler;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.SyncListener;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;

import java.lang.reflect.Field;
import java.util.List;


/**
 * Created by caijian on 15-9-10.
 */

/**
 * 意见反馈Dialog
 */
public class FeedbackDialog {
    public static void show(final Context context) {
        final Conversation mComversation = new FeedbackAgent(context).getDefaultConversation();
        LayoutInflater inflater = LayoutInflater.from(context);
        final View layout = inflater.inflate(R.layout.dialog_feedback, null);
        final EditText et_feedback = (EditText) layout.findViewById(R.id.dialog_feedback_et1);
        final EditText et_contact = (EditText) layout.findViewById(R.id.dialog_feedback_et2);
        final RadioGroup rg1 = (RadioGroup) layout.findViewById(R.id.dialog_feedback_rg1);
        final RadioGroup rg2 = (RadioGroup) layout.findViewById(R.id.dialog_feedback_rg2);
        final RadioButton rb1 = (RadioButton) layout.findViewById(R.id.dialog_feedback_rb1);
        final RadioButton rb2 = (RadioButton) layout.findViewById(R.id.dialog_feedback_rb2);
        final RadioButton rb3 = (RadioButton) layout.findViewById(R.id.dialog_feedback_rb3);
        final RadioButton rb4 = (RadioButton) layout.findViewById(R.id.dialog_feedback_rb4);
        final RadioButton rb5 = (RadioButton) layout.findViewById(R.id.dialog_feedback_rb5);
        final RadioButton rb6 = (RadioButton) layout.findViewById(R.id.dialog_feedback_rb6);
        AlertDialog.Builder mDialog = new AlertDialog.Builder(context);
        mDialog
//                .setCancelable(false)
                .setView(layout).setNegativeButton("取消", new DialogInterface.OnClickListener() {
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
                        String reason;
                        String feedback = et_feedback.getText().toString();
                        String contact = et_contact.getText().toString();
                        if (rb1.isChecked()) {
                            reason = rb1.getTag().toString();
                        } else if (rb2.isChecked()) {
                            reason = rb2.getTag().toString();
                        } else if (rb3.isChecked()) {
                            reason = rb3.getTag().toString();
                        } else if (rb4.isChecked()) {
                            reason = rb4.getTag().toString();
                        } else if (rb5.isChecked()) {
                            reason = rb5.getTag().toString();
                        } else if (rb6.isChecked()) {
                            reason = rb6.getTag().toString();
                        } else {
                            reason = "没有理由";
                        }
                        if (reason.equals("没有理由") && feedback.isEmpty()) {
//                            try {
//                                Field field = dialog.getClass().getDeclaredField("mAlert");
//                                field.setAccessible(true);
//                                //获得mAlert变量的值
//                                Object obj = field.get(dialog);
//                                field = obj.getClass().getDeclaredField("mHandler");
//                                field.setAccessible(true);
//                                //修改mHandler变量的值，使用新的ButtonHandler类
//                                field.set(obj, new ButtonHandler(dialog));
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
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
        RadioGroup.OnCheckedChangeListener lis = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.dialog_feedback_rb1 == group.getCheckedRadioButtonId()) {
                    if (rb1.isChecked()) {
                        rg2.clearCheck();
                    }
                } else if (R.id.dialog_feedback_rb2 == group.getCheckedRadioButtonId()) {
                    if (rb2.isChecked()) {
                        rg2.clearCheck();
                    }
                } else if (R.id.dialog_feedback_rb3 == group.getCheckedRadioButtonId()) {
                    if (rb3.isChecked()) {
                        rg2.clearCheck();
                    }
                } else if (R.id.dialog_feedback_rb4 == group.getCheckedRadioButtonId()) {
                    if (rb4.isChecked()) {
                        rg1.clearCheck();
                    }
                } else if (R.id.dialog_feedback_rb5 == group.getCheckedRadioButtonId()) {
                    if (rb5.isChecked()) {
                        rg1.clearCheck();
                    }
                } else if (R.id.dialog_feedback_rb6 == group.getCheckedRadioButtonId()) {
                    if (rb6.isChecked()) {
                        rg1.clearCheck();
                    }
                }
            }
        };
        rg1.setOnCheckedChangeListener(lis);
        rg2.setOnCheckedChangeListener(lis);
    }
}

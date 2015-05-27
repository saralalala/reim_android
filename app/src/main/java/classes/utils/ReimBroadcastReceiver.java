package classes.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.rushucloud.reim.R;
import com.rushucloud.reim.me.MessageActivity;
import com.rushucloud.reim.report.ApproveReportActivity;
import com.rushucloud.reim.report.CommentActivity;
import com.rushucloud.reim.report.EditReportActivity;
import com.rushucloud.reim.report.ShowReportActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

import classes.model.Apply;
import classes.model.Invite;
import classes.model.Message;
import classes.model.Report;
import netUtils.NetworkConstant;

public class ReimBroadcastReceiver extends BroadcastReceiver
{
    public static final int REPORT_MINE_REJECTED = 0;
    public static final int REPORT_MINE_REJECTED_WITH_COMMENT = 1;
    public static final int REPORT_MINE_APPROVED = 2;
    public static final int REPORT_MINE_FINISHED = 3;
    public static final int REPORT_MINE_SUBMMITED_ONLY_COMMENT = 4;
    public static final int REPORT_MINE_REJECTED_ONLY_COMMENT = 5;
    public static final int REPORT_MINE_APPROVED_ONLY_COMMENT = 6;
    public static final int REPORT_MINE_FINISHED_ONLY_COMMENT = 7;
    public static final int REPORT_OTHERS_SUBMMITED = 8;
    public static final int REPORT_OTHERS_SUBMMITED_CC = 9;
    public static final int REPORT_OTHERS_CAN_BE_APPROVED_ONLY_COMMENT = 10;
    public static final int REPORT_OTHERS_SUBMITTED_ONLY_COMMENT = 11;
    public static final int REPORT_OTHERS_REJECTED_ONLY_COMMENT = 12;
    public static final int REPORT_OTHERS_APPROVED_ONLY_COMMENT = 13;

    private static NotificationManager manager = null;
    private static int notificationID = 0;

    public void onReceive(Context context, Intent intent)
    {
        try
        {
            String action = intent.getAction();
            if (action.equals("com.avos.UPDATE_STATUS"))
            {
                notificationID++;
                JSONObject jObject = new JSONObject(intent.getExtras().getString("com.avos.avoscloud.Data"));
                int type = jObject.getInt("type");
                String message = jObject.getString("msg");

                System.out.println("ReimBroadcastReceiver:" + jObject.toString());
                Intent notificationIntent = new Intent("com.rushucloud.reim.NOTIFICATION_CLICKED");
                notificationIntent.putExtra("type", type);
                notificationIntent.putExtra("data", jObject.toString());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationID, notificationIntent,
                                                                         PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setSmallIcon(R.drawable.ic_launcher);
                builder.setContentTitle(context.getString(R.string.app_name));
                builder.setContentText(message);
                builder.setDefaults(NotificationCompat.DEFAULT_ALL);
                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);

                Notification notification = builder.build();

                if (PhoneUtils.isMIUIV6())
                {
                    try
                    {
                        int number = ReimApplication.getMineUnreadList().size() + ReimApplication.getOthersUnreadList().size() +
                                ReimApplication.getUnreadMessagesCount() + 1;
                        Class miuiNotificationClass = Class.forName("android.app.MiuiNotification");
                        Object miuiNotification = miuiNotificationClass.newInstance();
                        Field field = miuiNotification.getClass().getDeclaredField("messageCount");
                        field.setAccessible(true);
                        field.set(miuiNotification, number);
                        field = notification.getClass().getField("extraNotification");
                        field.set(notification, miuiNotification);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                if (manager == null)
                {
                    manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                }
                manager.notify(notificationID, notification);
            }
            else if (action.equals("com.rushucloud.reim.NOTIFICATION_CLICKED"))
            {
                int type = intent.getIntExtra("type", -1);
                JSONObject jObject = new JSONObject(intent.getStringExtra("data"));

                if (type == NetworkConstant.PUSH_TYPE_SYSTEM_MESSAGE)
                {
                    ViewUtils.showToast(context, jObject.getString("message"));
                }
                else if (type == NetworkConstant.PUSH_TYPE_REPORT)
                {
                    boolean myReport = jObject.getInt("uid") == AppPreference.getAppPreference().getCurrentUserID();

                    Report report = new Report();
                    report.setServerID(jObject.getInt("args"));

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("report", report);
                    bundle.putBoolean("fromPush", true);
                    bundle.putBoolean("myReport", myReport);

                    Intent newIntent = new Intent();
                    int pushType = classifyReportType(jObject);
                    if (pushType == REPORT_MINE_REJECTED || pushType == REPORT_MINE_REJECTED_WITH_COMMENT)
                    {
                        if (pushType == REPORT_MINE_REJECTED_WITH_COMMENT)
                        {
                            bundle.putBoolean("commentPrompt", true);
                        }
                        newIntent.setClass(context, EditReportActivity.class);
                    }
                    else if (pushType == REPORT_MINE_APPROVED || pushType == REPORT_MINE_FINISHED || pushType == REPORT_OTHERS_SUBMMITED_CC)
                    {
                        newIntent.setClass(context, ShowReportActivity.class);
                    }
                    else if (pushType == REPORT_OTHERS_SUBMMITED)
                    {
                        newIntent.setClass(context, ApproveReportActivity.class);
                    }
                    else
                    {
                        bundle.putInt("pushType", pushType);
                        newIntent.setClass(context, CommentActivity.class);
                    }

                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    newIntent.putExtras(bundle);
                    context.startActivity(newIntent);
                }
                else if (type == NetworkConstant.PUSH_TYPE_INVITE || type == NetworkConstant.PUSH_TYPE_INVITE_REPLY)
                {
                    Invite invite = new Invite();
                    try
                    {
                        invite.setType(Message.TYPE_INVITE);
                        invite.setTitle(jObject.getString("msg"));
                        invite.setContent(jObject.getString("msg"));
                        invite.setInviteCode(jObject.getString("code"));
                        invite.setTypeCode(jObject.getInt("actived"));
                    }
                    catch (JSONException e)
                    {
                        invite.setTitle(context.getString(R.string.failed_to_read_data));
                        invite.setContent(context.getString(R.string.failed_to_read_data));
                        invite.setInviteCode("");
                        invite.setTypeCode(Invite.TYPE_REJECTED);
                    }

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("message", invite);
                    bundle.putBoolean("fromPush", true);

                    Intent newIntent = new Intent(context, MessageActivity.class);
                    newIntent.putExtras(bundle);
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(newIntent);
                }
                else if (type == NetworkConstant.PUSH_TYPE_ADMIN_MESSAGE)
                {
                    Message message = new Message();
                    try
                    {
                        message.setType(Message.TYPE_MESSAGE);
                        message.setTitle(jObject.getString("msg"));
                        message.setServerID(jObject.getInt("args"));
                    }
                    catch (JSONException e)
                    {
                        message.setTitle(context.getString(R.string.failed_to_read_data));
                        message.setContent(context.getString(R.string.failed_to_read_data));
                    }

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("message", message);
                    bundle.putBoolean("fromPush", true);

                    Intent newIntent = new Intent(context, MessageActivity.class);
                    newIntent.putExtras(bundle);
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(newIntent);
                }
                else if (type == NetworkConstant.PUSH_TYPE_APPLY || type == NetworkConstant.PUSH_TYPE_APPLY_REPLY)
                {
                    Apply apply = new Apply();
                    try
                    {
                        apply.setType(Message.TYPE_APPLY);
                        apply.setTitle(jObject.getString("msg"));
                        apply.setContent(jObject.getString("msg"));
                        apply.setTypeCode(jObject.getInt("permit"));
                        apply.setServerID(jObject.getInt("args"));
                    }
                    catch (JSONException e)
                    {
                        apply.setTitle(context.getString(R.string.failed_to_read_data));
                        apply.setContent(context.getString(R.string.failed_to_read_data));
                        apply.setTypeCode(Apply.TYPE_REJECTED);
                    }

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("message", apply);
                    bundle.putBoolean("fromPush", true);

                    Intent newIntent = new Intent(context, MessageActivity.class);
                    newIntent.putExtras(bundle);
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(newIntent);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private int classifyReportType(JSONObject jObject)
    {
        try
        {
            boolean myReport = jObject.getInt("uid") == AppPreference.getAppPreference().getCurrentUserID();
            boolean hasComment = Utils.intToBoolean(jObject.getInt("comment_flag"));
            boolean onlyComment = Utils.intToBoolean(jObject.getInt("only_comment"));
            boolean isCC = Utils.intToBoolean(jObject.getInt("cc_flag"));
            int status = jObject.getInt("status");
            int myDecision = jObject.getInt("my_decision");

            if (myReport && !hasComment && status == Report.STATUS_REJECTED)
            {
                return REPORT_MINE_REJECTED;
            }
            else if (myReport && hasComment && !onlyComment && status == Report.STATUS_REJECTED)
            {
                return REPORT_MINE_REJECTED_WITH_COMMENT;
            }
            else if (myReport && !hasComment && status == Report.STATUS_APPROVED)
            {
                return REPORT_MINE_APPROVED;
            }
            else if (myReport && !hasComment && status == Report.STATUS_FINISHED)
            {
                return REPORT_MINE_FINISHED;
            }
            else if (myReport && hasComment && status == Report.STATUS_SUBMITTED)
            {
                return REPORT_MINE_SUBMMITED_ONLY_COMMENT;
            }
            else if (myReport && hasComment && onlyComment && status == Report.STATUS_REJECTED)
            {
                return REPORT_MINE_REJECTED_ONLY_COMMENT;
            }
            else if (myReport && hasComment && status == Report.STATUS_APPROVED)
            {
                return REPORT_MINE_APPROVED_ONLY_COMMENT;
            }
            else if (myReport && hasComment && status == Report.STATUS_FINISHED)
            {
                return REPORT_MINE_FINISHED_ONLY_COMMENT;
            }
            else if (!myReport && !hasComment && !isCC && status == Report.STATUS_SUBMITTED && myDecision == Report.STATUS_SUBMITTED)
            {
                return REPORT_OTHERS_SUBMMITED;
            }
            else if (!myReport && !hasComment && isCC && status == Report.STATUS_SUBMITTED)
            {
                return REPORT_OTHERS_SUBMMITED_CC;
            }
            else if (!myReport && hasComment && !isCC && status == Report.STATUS_SUBMITTED && myDecision == Report.STATUS_SUBMITTED)
            {
                return REPORT_OTHERS_CAN_BE_APPROVED_ONLY_COMMENT;
            }
            else if (!myReport && hasComment && status == Report.STATUS_SUBMITTED)
            {
                return REPORT_OTHERS_SUBMITTED_ONLY_COMMENT;
            }
            else if (!myReport && hasComment && status == Report.STATUS_REJECTED)
            {
                return REPORT_OTHERS_REJECTED_ONLY_COMMENT;
            }
            else if (!myReport && hasComment && status == Report.STATUS_APPROVED)
            {
                return REPORT_OTHERS_APPROVED_ONLY_COMMENT;
            }
            else
            {
                return -1;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return -1;
        }
    }
}

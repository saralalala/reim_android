package classes.utils;

import org.json.JSONException;
import org.json.JSONObject;

import classes.Invite;
import classes.Report;

import com.rushucloud.reim.R;
import com.rushucloud.reim.me.MessageDetailActivity;
import com.rushucloud.reim.report.ApproveReportActivity;
import com.rushucloud.reim.report.CommentActivity;
import com.rushucloud.reim.report.EditReportActivity;
import com.rushucloud.reim.report.ShowReportActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ReimBroadcastReceiver extends BroadcastReceiver
{
	private static final int TYPE_SYSTEM_MESSAGE = 1;
	private static final int TYPE_REPORT = 2;
	private static final int TYPE_INVITE = 3;
	private static final int TYPE_INVITE_REPLY = 4;

	public static final int REPORT_MINE_REJECTED = 0;
	public static final int REPORT_MINE_REJECTED_WITH_COMMENT = 1;
	public static final int REPORT_MINE_APPROVED = 2;
	public static final int REPORT_MINE_SUBMMITED_ONLY_COMMENT = 3;
	public static final int REPORT_MINE_REJECTED_ONLY_COMMENT = 4;
	public static final int REPORT_MINE_APPROVED_ONLY_COMMENT = 5;
	public static final int REPORT_OTHERS_SUBMMITED = 6;
	public static final int REPORT_OTHERS_SUBMMITED_CC = 7;
	public static final int REPORT_OTHERS_CAN_BE_APPROVED_ONLY_COMMENT = 8;
	public static final int REPORT_OTHERS_SUBMITTED_ONLY_COMMENT = 9;
	public static final int REPORT_OTHERS_REJECTED_ONLY_COMMENT = 10;
	public static final int REPORT_OTHERS_APPROVED_ONLY_COMMENT = 11;
	
	private static NotificationManager manager = null;
	private static int messageNumber = 0;
	
	@SuppressWarnings("deprecation")
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			String action = intent.getAction();
			if (action.equals("com.avos.UPDATE_STATUS"))
			{
				messageNumber++;
				JSONObject jObject = new JSONObject(intent.getExtras().getString("com.avos.avoscloud.Data"));
				int type = jObject.getInt("type");
				String message = jObject.getString("msg");

				System.out.println(jObject.toString());
				Intent notificationIntent = new Intent("com.rushucloud.reim.NOTIFICATION_CLICKED");
				notificationIntent.putExtra("type",  type);
				notificationIntent.putExtra("data", jObject.toString());
				
				Notification notification = new Notification();
				notification.icon = R.drawable.ic_launcher;
				notification.tickerText = message;
				notification.defaults = Notification.DEFAULT_ALL;
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 
																PendingIntent.FLAG_UPDATE_CURRENT);
				
				notification.setLatestEventInfo(context, context.getString(R.string.app_name), message, pendingIntent);

				if (manager == null)
				{
					manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
				}
				manager.notify(messageNumber, notification);
			}
			else if (action.equals("com.rushucloud.reim.NOTIFICATION_CLICKED"))
			{
				int type = intent.getIntExtra("type", -1);
				JSONObject jObject = new JSONObject(intent.getStringExtra("data"));
				
				if (type == TYPE_SYSTEM_MESSAGE)
				{
					ViewUtils.showToast(context, jObject.getString("message"));
				}
				else if (type == TYPE_REPORT)
				{
					boolean myReport = jObject.getInt("uid") == AppPreference.getAppPreference().getCurrentUserID();
					
					Report report = new Report();
					report.setServerID(jObject.getInt("args"));

					Bundle bundle = new Bundle();
					bundle.putSerializable("report", report);
					bundle.putBoolean("fromPush", true);
					bundle.putBoolean("myReport", myReport);

					Intent newIntent = new Intent();
					int pushType = judgeReportType(jObject);
					if (pushType == REPORT_MINE_REJECTED || pushType == REPORT_MINE_REJECTED_WITH_COMMENT)
					{
						if (pushType == REPORT_MINE_REJECTED_WITH_COMMENT)
						{
							bundle.putBoolean("commentPrompt", true);
						}
						newIntent.setClass(context, EditReportActivity.class);						
					}
					else if (pushType == REPORT_MINE_APPROVED || pushType == REPORT_OTHERS_SUBMMITED_CC)
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
				else if (type == TYPE_INVITE || type == TYPE_INVITE_REPLY)
				{
					Invite invite = new Invite();
					try
					{
						invite.setMessage(jObject.getString("msg"));
						invite.setInviteCode(jObject.getString("code"));
						invite.setTypeCode(jObject.getInt("actived"));
					}
					catch (JSONException e)
					{
						invite.setMessage(context.getString(R.string.prompt_data_error));
						invite.setInviteCode("");
					}
					
					Bundle bundle = new Bundle();
					bundle.putSerializable("invite", invite);
					bundle.putBoolean("fromPush", true);
					
					Intent newIntent = new Intent(context, MessageDetailActivity.class);
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
	
	private int judgeReportType(JSONObject jObject)
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

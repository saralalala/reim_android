package classes;

import org.json.JSONException;
import org.json.JSONObject;

import com.rushucloud.reim.ApproveReportActivity;
import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.rushucloud.reim.me.InvitedActivity;

import database.DBManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ReimBroadcastReceiver extends BroadcastReceiver
{
	private static final int TYPE_SYSTEM_MESSAGE = 1;
	private static final int TYPE_REPORT = 2;
	private static final int TYPE_INVITE = 3;
	
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
				
				Intent notificationIntent = new Intent("com.rushucloud.reim.NOTIFICATION_CLICKED");
				notificationIntent.putExtra("type", type);
				notificationIntent.putExtra("data", jObject.toString());
				
				Notification notification = new Notification();
				notification.icon = R.drawable.default_avatar;
				notification.tickerText = message;
				notification.defaults = Notification.DEFAULT_ALL;
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 
																PendingIntent.FLAG_UPDATE_CURRENT);
				notification.setLatestEventInfo(context, "如数云报销", message, pendingIntent);

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
				switch (type)
				{
					case TYPE_SYSTEM_MESSAGE:
					{
						Toast.makeText(context, jObject.getString("message"), Toast.LENGTH_SHORT).show();
						break;
					}		
					case TYPE_REPORT:
					{
						int status = jObject.getInt("status");
						if (status == 1)
						{
							Intent newIntent = new Intent(context, ApproveReportActivity.class);
							newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							newIntent.putExtra("reportID", jObject.getInt("args"));
							context.startActivity(newIntent);
						}
						else 
						{
							Bundle bundle = new Bundle();
							bundle.putInt("tabIndex", 1);
							Intent newIntent = new Intent(context, MainActivity.class);
							newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							newIntent.putExtras(bundle);
							context.startActivity(newIntent);							
						}
						break;
					}
					case TYPE_INVITE:
					{
						Intent newIntent = new Intent(context, InvitedActivity.class);
						newIntent.putExtra("data", jObject.toString());
						newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(newIntent);
						break;
					}
					default:
						break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void updateReport(JSONObject jObject)
	{
		try
		{
			int status = Integer.valueOf(jObject.getString("status"));
			int reportID = Integer.valueOf(jObject.getString("args"));
			if (status == 1)
			{
				DBManager.getDBManager().insertApproveReport(reportID, AppPreference.getAppPreference().getCurrentUserID());
			}
		}
		catch (NumberFormatException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

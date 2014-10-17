package classes;

import org.json.JSONObject;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.rushucloud.reim.start.WelcomeActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReimBroadcastReceiver extends BroadcastReceiver
{
	private static final int TYPE_INVITE = 0;
	private static final int TYPE_MY_REPORT = 1;
	private static final int TYPE_APPROVE_REPORT = 2;
	private static final int TYPE_SYSTEM_MESSAGE = 3;
	
	private static NotificationManager manager = null;
	private static int messageNumber = 0;
	private static int type = 0;
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
				type = jObject.getInt("type");
				
				switch (type)
				{
					case TYPE_INVITE:
						updateInvite(jObject);
						break;
					case TYPE_MY_REPORT:
						updateReport(jObject);
						break;
					case TYPE_APPROVE_REPORT:
						updateReport(jObject);
						break;
					case TYPE_SYSTEM_MESSAGE:
						updateSystemMessage(jObject);
						break;						
					default:
						break;
				}
				
				Intent notificationIntent = new Intent("com.rushucloud.reim.NOTIFICATION_CLICKED");
				Notification notification = new Notification();
				notification.icon = R.drawable.default_avatar;
				notification.tickerText = "您收到了一条消息!";
				notification.defaults = Notification.DEFAULT_ALL;
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
				notification.setLatestEventInfo(context, "如数云报销", "共收到"+messageNumber+"条信息", pendingIntent);
				manager.notify(0, notification);
			}
			else if (action.equals("com.rushucloud.reim.NOTIFICATION_CLICKED"))
			{				
				switch (type)
				{
					case TYPE_INVITE:
					{
						Intent newIntent = new Intent(context, WelcomeActivity.class);
						newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(newIntent);
						break;
					}
					case TYPE_MY_REPORT:
					{
						Intent newIntent = new Intent(context, MainActivity.class);
						newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						newIntent.putExtra("reportTabIndex", 0);
						context.startActivity(newIntent);
						break;
					}
					case TYPE_APPROVE_REPORT:
					{
						Intent newIntent = new Intent(context, MainActivity.class);
						newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						newIntent.putExtra("reportTabIndex", 1);
						context.startActivity(newIntent);
						break;
					}
					case TYPE_SYSTEM_MESSAGE:
					{
						break;
					}		
					default:
						break;
				}

				messageNumber = 0;
				type = 0;
			}
			else if (action.equals("android.intent.action.BOOT_COMPLETED"))
			{
				if (manager == null)
				{
					manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
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
		
	}
	
	private void updateInvite(JSONObject jObject)
	{
		
	}
	
	private void updateSystemMessage(JSONObject jObject)
	{
		
	}
}

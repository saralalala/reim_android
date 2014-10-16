package classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReimBroadcastReceiver extends BroadcastReceiver
{	
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			String action = intent.getAction();
			if (action.equals("com.avos.UPDATE_STATUS"))
			{
	            String channel = intent.getExtras().getString("com.avos.avoscloud.Channel");
				System.out.println(action + "  " + channel);
				
				//TODO GET REPORT INFO
				// REQUEST FOR REPORT
				// SET ALERT
			}
			else if (action.equals("android.intent.action.BOOT_COMPLETED"))
			{
				
			}
			else if (action.equals("android.intent.action.USER_PRESENT"))
			{
				
			}
			else // action.equals("android.net.conn.CONNECTIVITY_CHANGE"))
			{
				
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

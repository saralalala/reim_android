package classes;

import java.util.Iterator;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ReimBroadcastReceiver extends BroadcastReceiver
{
	private static final String TAG = "ReimBroadcastReceiver";
	
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			String action = intent.getAction();
			Bundle bundle = intent.getExtras();
			String channel = bundle.getString("com.avos.avoscloud.Channel");
			JSONObject jsonObject = new JSONObject(bundle.getString("com.avos.avoscloud.Data"));

            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
            
			Iterator<?> iterator = jsonObject.keys();
			while (iterator.hasNext())
			{
				String key = (String)iterator.next();
                Log.d(TAG, "..." + key + " => " + jsonObject.getString(key));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

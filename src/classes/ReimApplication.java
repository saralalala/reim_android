package classes;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;

public class ReimApplication extends Application
{
	public void onCreate()
	{
		super.onCreate();
		readAppPreference();
	}
	
	private void readAppPreference()
	{
		SharedPreferences preferences = getSharedPreferences("ReimApplication", MODE_PRIVATE);
		AppPreference appPreference = AppPreference.getAppPreference();
		appPreference.setUsername(preferences.getString("username", ""));
		appPreference.setPassword(preferences.getString("password", ""));
		appPreference.setDeviceToken(preferences.getString("deviceToken", ""));
		appPreference.setServerToken(preferences.getString("serverToken", ""));
		appPreference.setCacheDirectory(this.getCacheDir().getAbsolutePath());	
		
		if (appPreference.getDeviceToken().equals(""))
		{
			TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
			appPreference.setDeviceToken(telephonyManager.getDeviceId());
		}
	}
	
	public void saveAppPreference()
	{
		SharedPreferences appPreference = getSharedPreferences("ReimApplication", MODE_PRIVATE);
		AppPreference userInfo = AppPreference.getAppPreference();
		Editor editor = appPreference.edit();
		editor.putString("username", userInfo.getUsername());
		editor.putString("password", userInfo.getPassword());
		editor.putString("deviceToken", userInfo.getDeviceToken());
		editor.putString("serverToken", userInfo.getServerToken());
		editor.commit();
	}
}
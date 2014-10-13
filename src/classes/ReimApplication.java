package classes;

import java.io.File;

import database.DBManager;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.telephony.TelephonyManager;

public class ReimApplication extends Application
{	
	public void onCreate()
	{
		super.onCreate();
		readAppPreference();
		createDirectories();
		DBManager.createDBManager(getApplicationContext());
	}
	
	private void readAppPreference()
	{
		SharedPreferences preferences = getSharedPreferences("ReimApplication", MODE_PRIVATE);
		AppPreference appPreference = AppPreference.getAppPreference();
		appPreference.setUsername(preferences.getString("username", ""));
		appPreference.setPassword(preferences.getString("password", ""));
		appPreference.setDeviceToken(preferences.getString("deviceToken", ""));
		appPreference.setServerToken(preferences.getString("serverToken", ""));
		appPreference.setSyncWithoutWifi(preferences.getBoolean("syncWithoutWifi", false));
		
		String path = Environment.getExternalStorageDirectory() + "/如数云报销";
		appPreference.setProfileImageDirectory(path + "/images/profile");
		appPreference.setInvoiceImageDirectory(path + "/images/invoice");
		
		if (appPreference.getDeviceToken().equals(""))
		{
			TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
			appPreference.setDeviceToken(telephonyManager.getDeviceId());
		}
		
		appPreference.setSyncWithoutWifi(preferences.getBoolean("syncWithoutWifi", false));
		appPreference.setEnablePasswordProtection(preferences.getBoolean("enablePasswordProtection", false));
	}
	
	public void saveAppPreference()
	{
		SharedPreferences sharedPreference = getSharedPreferences("ReimApplication", MODE_PRIVATE);
		AppPreference appPreference = AppPreference.getAppPreference();
		Editor editor = sharedPreference.edit();
		editor.putString("username", appPreference.getUsername());
		editor.putString("password", appPreference.getPassword());
		editor.putString("deviceToken", appPreference.getDeviceToken());
		editor.putString("serverToken", appPreference.getServerToken());
		editor.putBoolean("syncWithoutWifi", appPreference.syncWithoutWifi());
		editor.putBoolean("enablePasswordProtection", appPreference.passwordProtectionEnabled());
		editor.commit();
	}
	
	private void createDirectories()
	{
		String appDirectory = Environment.getExternalStorageDirectory() + "/如数云报销";
		File dir = new File(appDirectory);
		if (!dir.exists())
		{
			dir.mkdir();
		}
		dir = new File(appDirectory + "/images");
		if (!dir.exists())
		{
			dir.mkdir();
		}
		dir = new File(appDirectory + "/images/profile");
		if (!dir.exists())
		{
			dir.mkdir();
		}
		dir = new File(appDirectory + "/images/invoice");
		if (!dir.exists())
		{
			dir.mkdir();
		}		
	}
}
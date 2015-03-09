package classes.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Environment;

import com.avos.avoscloud.AVInstallation;

import java.io.File;

import classes.Group;
import classes.User;

public class AppPreference
{
	private static AppPreference appPreference = null;
	private Context context = null;
	
	private int currentUserID = -1;
	private int currentGroupID = -1;
	private String username = "";
	private String password = "";
	private String deviceToken = "";
	private String serverToken = "";
	private boolean syncOnlyWithWifi = true;
	private boolean enablePasswordProtection = true;
	private int lastSyncTime = 0;
	private int lastGetStatTime = 0;
	private String appDirectory = "";
	private String appImageDirectory = "";
	private String avatarImageDirectory = "";
	private String invoiceImageDirectory = "";
	private String iconImageDirectory = "";
	
	private AppPreference(Context context)
	{
		this.context = context;
	}
	
	public static synchronized void createAppPreference(Context context)
	{
		if (appPreference == null)
		{
			appPreference = new AppPreference(context);
			appPreference.readAppPreference();
		}
	}
	
	public static AppPreference getAppPreference()
	{
		return appPreference;
	}

	public void readAppPreference()
	{
		SharedPreferences preferences = context.getSharedPreferences("ReimApplication", Application.MODE_PRIVATE);
		appPreference.setCurrentUserID(preferences.getInt("currentUserID", -1));
		appPreference.setCurrentGroupID(preferences.getInt("currentGroupID", -1));
		appPreference.setUsername(preferences.getString("username", ""));
		appPreference.setPassword(preferences.getString("password", ""));
		appPreference.setDeviceToken(AVInstallation.getCurrentInstallation().getInstallationId());
		appPreference.setServerToken(preferences.getString("serverToken", ""));
		appPreference.setSyncOnlyWithWifi(preferences.getBoolean("syncOnlyWithWifi", true));
		appPreference.setEnablePasswordProtection(preferences.getBoolean("enablePasswordProtection", true));
		appPreference.setLastSyncTime(preferences.getInt("lastSyncTime", 0));
		appPreference.setLastGetStatTime(preferences.getInt("lastGetStatTime", 0));
		
		appPreference.setAppDirectory(Environment.getExternalStorageDirectory() + "/如数云报销");
		appPreference.setAppImageDirectory(appPreference.getAppDirectory() + "/images");
		appPreference.setAvatarImageDirectory(appPreference.getAppImageDirectory() + "/avatar");
		appPreference.setInvoiceImageDirectory(appPreference.getAppImageDirectory() + "/invoice");
		appPreference.setIconImageDirectory(appPreference.getAppImageDirectory() + "/icon");
	}
	
	public void saveAppPreference()
	{
		SharedPreferences sharedPreference = context.getSharedPreferences("ReimApplication", Application.MODE_PRIVATE);
		AppPreference appPreference = AppPreference.getAppPreference();
		Editor editor = sharedPreference.edit();
		editor.putInt("currentUserID", appPreference.getCurrentUserID());
		editor.putInt("currentGroupID", appPreference.getCurrentGroupID());
		editor.putString("username", appPreference.getUsername());
		editor.putString("password", appPreference.getPassword());
		editor.putString("deviceToken", appPreference.getDeviceToken());
		editor.putString("serverToken", appPreference.getServerToken());
		editor.putBoolean("syncOnlyWithWifi", appPreference.syncOnlyWithWifi());
		editor.putBoolean("enablePasswordProtection", appPreference.passwordProtectionEnabled());
		editor.putInt("lastSyncTime", appPreference.getLastSyncTime());
		editor.putInt("lastGetStatTime", appPreference.getLastGetStatTime());
		editor.commit();
	}
	
	public int getCurrentUserID()
	{
		return currentUserID;
	}
	public void setCurrentUserID(int currentUserID)
	{
		this.currentUserID = currentUserID;
	}	
	public User getCurrentUser()
	{
		return DBManager.getDBManager().getUser(currentUserID);
	}

	public int getCurrentGroupID()
	{
		return currentGroupID;
	}
	public void setCurrentGroupID(int currentGroupID)
	{
		this.currentGroupID = currentGroupID;
	}	
	public Group getCurrentGroup()
	{
		return DBManager.getDBManager().getGroup(currentGroupID);
	}
	
	public String getUsername()
	{
		return username;
	}
	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public String getPassword()
	{
		return password;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public String getDeviceToken()
	{
		return deviceToken;
	}
	public void setDeviceToken(String deviceToken)
	{
		this.deviceToken = deviceToken;
	}
	
	public String getServerToken()
	{
		return serverToken;
	}
	public void setServerToken(String serverToken)
	{
		this.serverToken = serverToken;
	}

	public boolean syncOnlyWithWifi()
	{
		return syncOnlyWithWifi;
	}
	public void setSyncOnlyWithWifi(boolean syncOnlyWithWifi)
	{
		this.syncOnlyWithWifi = syncOnlyWithWifi;
	}

	public boolean passwordProtectionEnabled()
	{
		return enablePasswordProtection;
	}
	public void setEnablePasswordProtection(boolean enablePasswordProtection)
	{
		this.enablePasswordProtection = enablePasswordProtection;
	}
	
	public int getLastSyncTime()
	{
		return lastSyncTime;
	}
	public void setLastSyncTime(int lastSyncTime)
	{
		this.lastSyncTime = lastSyncTime;
	}
	
	public int getLastGetStatTime()
	{
		return lastGetStatTime;
	}
	public void setLastGetStatTime(int lastGetStatTime)
	{
		this.lastGetStatTime = lastGetStatTime;
	}

	public String getAppDirectory()
	{
		return appDirectory;
	}
	public void setAppDirectory(String appDirectory)
	{
		this.appDirectory = appDirectory;
	}

	public String getAppImageDirectory()
	{
		return appImageDirectory;
	}
	public void setAppImageDirectory(String appImageDirectory)
	{
		this.appImageDirectory = appImageDirectory;
	}

	public String getAvatarImageDirectory()
	{
		return avatarImageDirectory;
	}
	public void setAvatarImageDirectory(String avatarImageDirectory)
	{
		this.avatarImageDirectory = avatarImageDirectory;
	}

	public String getTempAvatarPath()
	{
		return getAvatarImageDirectory() + "/temp.jpg";
	}
	public Uri getTempAvatarUri()
	{
		return Uri.fromFile(new File(getAvatarImageDirectory() + "/temp.jpg"));
	}
	
	public String getInvoiceImageDirectory()
	{
		return invoiceImageDirectory;
	}
	public void setInvoiceImageDirectory(String invoiceImageDirectory)
	{
		this.invoiceImageDirectory = invoiceImageDirectory;
	}

	public String getTempInvoicePath()
	{
		return getInvoiceImageDirectory() + "/temp.jpg";
	}
	public Uri getTempInvoiceUri()
	{
		return Uri.fromFile(new File(getInvoiceImageDirectory() + "/temp.jpg"));
	}
	
	public String getIconImageDirectory()
	{
		return iconImageDirectory;
	}
	public void setIconImageDirectory(String iconImageDirectory)
	{
		this.iconImageDirectory = iconImageDirectory;
	}
}

package classes;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.telephony.TelephonyManager;

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
	private String profileImageDirectory = "";
	private String invoiceImageDirectory = "";
	private boolean syncWithoutWifi = false;
	private boolean enablePasswordProtection = false;
	
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
			TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Application.TELEPHONY_SERVICE);
			appPreference.setDeviceToken(telephonyManager.getDeviceId());
		}
		
		appPreference.setSyncWithoutWifi(preferences.getBoolean("syncWithoutWifi", false));
		appPreference.setEnablePasswordProtection(preferences.getBoolean("enablePasswordProtection", false));
	}
	
	public void saveAppPreference()
	{
		SharedPreferences sharedPreference = context.getSharedPreferences("ReimApplication", Application.MODE_PRIVATE);
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
	
	public int getCurrentUserID()
	{
		return currentUserID;
	}

	public void setCurrentUserID(int currentUserID)
	{
		this.currentUserID = currentUserID;
	}	

	public int getCurrentGroupID()
	{
		return currentGroupID;
	}

	public void setCurrentGroupID(int currentGroupID)
	{
		this.currentGroupID = currentGroupID;
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

	public String getProfileImageDirectory()
	{
		return profileImageDirectory;
	}

	public void setProfileImageDirectory(String profileImageDirectory)
	{
		this.profileImageDirectory = profileImageDirectory;
	}

	public String getInvoiceImageDirectory()
	{
		return invoiceImageDirectory;
	}

	public void setInvoiceImageDirectory(String invoiceImageDirectory)
	{
		this.invoiceImageDirectory = invoiceImageDirectory;
	}

	public boolean syncWithoutWifi()
	{
		return syncWithoutWifi;
	}

	public void setSyncWithoutWifi(boolean syncWithoutWifi)
	{
		this.syncWithoutWifi = syncWithoutWifi;
	}

	public boolean passwordProtectionEnabled()
	{
		return enablePasswordProtection;
	}

	public void setEnablePasswordProtection(boolean enablePasswordProtection)
	{
		this.enablePasswordProtection = enablePasswordProtection;
	}
}

package classes;

import android.graphics.Bitmap;

public class AppPreference
{
	private static AppPreference appPreference = null;
	
	private int currentUserID = -1;
	private int currentGroupID = -1;
	private String username = "";
	private String password = "";
	private String deviceToken = "";
	private String serverToken = "";
	private String profileImageDirectory = "";
	private String invoiceImageDirectory = "";
	private boolean syncWithoutWifi = false;
	private Bitmap defaultInvoice = null;

	private AppPreference()
	{
		
	}
	
	public static AppPreference getAppPreference()
	{
		if (appPreference == null)
		{
			appPreference = new AppPreference();
		}
		return appPreference;
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

	public Bitmap getDefaultInvoice()
	{
		return defaultInvoice;
	}

	public void setDefaultInvoice(Bitmap defaultInvoice)
	{
		this.defaultInvoice = defaultInvoice;
	}
}

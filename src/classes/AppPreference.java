package classes;

public class AppPreference
{
	private static AppPreference appPreference = null;
	
	private int currentUserID = -1;
	private String username = "";
	private String password = "";
	private String deviceToken = "";
	private String serverToken = "";
	private String cacheDirectory = "";

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

	public String getCacheDirectory()
	{
		return cacheDirectory;
	}

	public void setCacheDirectory(String cacheDirectory)
	{
		this.cacheDirectory = cacheDirectory;
	}
}

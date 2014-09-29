package classes;

public class AppPreference
{
	private static AppPreference appPreference = null;
	
	private String email = "";
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
	
	public String getEmail()
	{
		return email;
	}
	
	public void setEmail(String email)
	{
		this.email = email;
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

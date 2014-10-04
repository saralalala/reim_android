package classes;

public class User
{
	private int id = -1;
	private String email = "";
	private String password = "";
	private String nickname = "";
	private String phone = "";
	private String avatarPath = "";
	private int privilege = 0;
	private Boolean isActive = false;
	private Boolean isAdmin = false;
	private int groupID = -1;
	private int defaultManagerID = -1;
	private int serverUpdatedDate = -1;
	private int localUpdatedDate = -1;
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
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
	
	public String getNickname()
	{
		return nickname;
	}
	public void setNickname(String nickname)
	{
		this.nickname = nickname;
	}
	
	public String getPhone()
	{
		return phone;
	}
	public void setPhone(String phone)
	{
		this.phone = phone;
	}
	
	public String getAvatarPath()
	{
		return avatarPath;
	}
	public void setAvatarPath(String avatarPath)
	{
		this.avatarPath = avatarPath;
	}
	
	public int getPrivilege()
	{
		return privilege;
	}
	public void setPrivilege(int privilege)
	{
		this.privilege = privilege;
	}
	
	public Boolean isActive()
	{
		return isActive;
	}
	public void setIsActive(Boolean isActive)
	{
		this.isActive = isActive;
	}
	
	public Boolean isAdmin()
	{
		return isAdmin;
	}
	public void setIsAdmin(Boolean isAdmin)
	{
		this.isAdmin = isAdmin;
	}
	
	public int getGroupID()
	{
		return groupID;
	}
	public void setGroupID(int groupID)
	{
		this.groupID = groupID;
	}
	
	public int getDefaultManagerID()
	{
		return defaultManagerID;
	}
	public void setDefaultManagerID(int defaultManagerID)
	{
		this.defaultManagerID = defaultManagerID;
	}
	
	public int getServerUpdatedDate()
	{
		return serverUpdatedDate;
	}
	public void setServerUpdatedDate(int serverUpdatedDate)
	{
		this.serverUpdatedDate = serverUpdatedDate;
	}
	
	public int getLocalUpdatedDate()
	{
		return localUpdatedDate;
	}
	public void setLocalUpdatedDate(int localUpdatedDate)
	{
		this.localUpdatedDate = localUpdatedDate;
	}
}

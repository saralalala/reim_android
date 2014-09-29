package classes;

import java.util.Date;

public class User
{
	private int id = -1;
	private String email = "";
	private String password = "";
	private String nickname = "";
	private String phone = "";
	private int privilege = -1;
	private Boolean isActive = false;
	private Boolean isAdmin = false;
	private int groupID = -1;
	private int defaultManagerID = -1;
	private Date serverUpdatedDate = null;
	private Date localUpdatedDate = null;
	
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
	
	public int getPrivilege()
	{
		return privilege;
	}
	public void setPrivilege(int privilege)
	{
		this.privilege = privilege;
	}
	
	public Boolean getIsActive()
	{
		return isActive;
	}
	public void setIsActive(Boolean isActive)
	{
		this.isActive = isActive;
	}
	
	public Boolean getIsAdmin()
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
	
	public Date getServerUpdatedDate()
	{
		return serverUpdatedDate;
	}
	public void setServerUpdatedDate(Date serverUpdatedDate)
	{
		this.serverUpdatedDate = serverUpdatedDate;
	}
	
	public Date getLocalUpdatedDate()
	{
		return localUpdatedDate;
	}
	public void setLocalUpdatedDate(Date localUpdatedDate)
	{
		this.localUpdatedDate = localUpdatedDate;
	}
}

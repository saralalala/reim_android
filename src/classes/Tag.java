package classes;

import java.sql.Date;

public class Tag
{
	private int id;
	private int groupID;
	private String name;
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
	
	public int getGroupID()
	{
		return groupID;
	}
	public void setGroupID(int groupID)
	{
		this.groupID = groupID;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
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

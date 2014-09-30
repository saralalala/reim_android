package classes;

public class Category
{
	private int id;
	private String name;
	private double limit;
	private int groupID;
	private int parentID;
	private Boolean isProveAhead;
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

	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}

	public double getLimit()
	{
		return limit;
	}
	public void setLimit(double limit)
	{
		this.limit = limit;
	}

	public int getGroupID()
	{
		return groupID;
	}
	public void setGroupID(int groupID)
	{
		this.groupID = groupID;
	}
	
	public int getParentID()
	{
		return parentID;
	}
	public void setParentID(int parentID)
	{
		this.parentID = parentID;
	}

	public Boolean isProveAhead()
	{
		return isProveAhead;
	}
	public void setIsProveAhead(Boolean isProveAhead)
	{
		this.isProveAhead = isProveAhead;
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
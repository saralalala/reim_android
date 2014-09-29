package classes;

import java.sql.Date;

public class Category
{
	private int id;
	private String name;
	private double limit;
	private int groupID;
	private int parentID;
	private Boolean preBillable;
	private Date lastUpdatedDate;

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
	
	public Boolean getPreBillable()
	{
		return preBillable;
	}
	public void setPreBillable(Boolean pb)
	{
		this.preBillable = pb;
	}

	public Date getLastUpdatedDate()
	{
		return lastUpdatedDate;
	}
	public void setLastUpdatedDate(Date lastUpdatedDate)
	{
		this.lastUpdatedDate = lastUpdatedDate;
	}
}
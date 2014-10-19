package classes;

import java.util.ArrayList;
import java.util.List;

public class Category
{
	private int serverID;
	private String name;
	private double limit;
	private int groupID;
	private int parentID;
	private Boolean isProveAhead;
	private int serverUpdatedDate = -1;
	private int localUpdatedDate = -1;

	public int getServerID()
	{
		return serverID;
	}
	public void setServerID(int serverID)
	{
		this.serverID = serverID;
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

	public static int getIndexOfCategory(List<Category> categoryList, Category category)
	{
		if (category == null)
		{
			return -1;
		}
		
		for (int i = 0; i < categoryList.size(); i++)
		{
			if (category.getServerID() == categoryList.get(i).getServerID())
			{
				return i;
			}
		}
		return -1;
	}
	
	public static String[] getCategoryNames(List<Category> categoryList)
	{
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < categoryList.size(); i++)
		{
			names.add(categoryList.get(i).getName() + "(MAX:￥" + categoryList.get(i).getLimit() + ")");
		}
		return names.toArray(new String[names.size()]);
	}
}
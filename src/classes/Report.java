package classes;

import java.util.List;

public class Report
{
	private String title = "";
	private int id = -1;
	private int itemCount = -1;
	private int createdDate = -1;
	private int serverUpdatedDate = -1;
	private int localUpdatedDate = -1;
	private double amount = -1;
	private int status = -1;
	private String itemIDs = "";
	private User user = null;
	
	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	
	public int getItemCount()
	{
		return itemCount;
	}
	public void setItemCount(int itemCount)
	{
		this.itemCount = itemCount;
	}
	
	public int getCreatedDate()
	{
		return createdDate;
	}
	public void setCreatedDate(int createdDate)
	{
		this.createdDate = createdDate;
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
	
	public double getAmount()
	{
		return amount;
	}
	public void setAmount(double amount)
	{
		this.amount = amount;
	}
	
	public int getStatus()
	{
		return status;
	}
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	public String getItemIDs()
	{
		return itemIDs;
	}
	public void setItemIDs(List<Item> itemList)
	{
		String itemIDString = "";
		int size = itemList.size();
		for (int i = 0; i < size; i++)
		{
			itemIDString += itemList.get(i).getId() + ",";
		}
		if (itemIDString.length() > 0)
		{
			itemIDString = itemIDString.substring(0, itemIDString.length()-1);
		}

		this.itemIDs = itemIDString;
	}
	
	public User getUser()
	{
		return user;
	}
	public void setUser(User user)
	{
		this.user = user;
	}
}

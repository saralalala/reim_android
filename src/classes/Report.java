package classes;

import java.util.ArrayList;
import java.util.List;

public class Report
{
	private String title = "";
	private int id = -1;
	private int localID = -1;
	private int itemCount = 0;
	private double amount = 0.0;
	private int status = 0;
	private List<Integer> itemList = null;
	private User user = null;
	private int createdDate = -1;
	private int serverUpdatedDate = -1;
	private int localUpdatedDate = -1;
	
	public Report()
	{
		itemList = new ArrayList<Integer>();
	}
	
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
	
	public int getLocalID()
	{
		return localID;
	}
	public void setLocalID(int localID)
	{
		this.localID = localID;
	}
	
	public int getItemCount()
	{
		return itemCount;
	}
	public void setItemCount(int itemCount)
	{
		this.itemCount = itemCount;
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
	
	public List<Integer> getItemList()
	{
		return itemList;
	}
	public void setItemList(List<Integer> itemList)
	{
		this.itemList = itemList;
	}
	
	public User getUser()
	{
		return user;
	}
	public void setUser(User user)
	{
		this.user = user;
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

	public void addItem(Item item)
	{
		itemCount++;
		amount += item.getAmount();
		itemList.add(item.getLocalID());
	}
	
	public void removeItem(Item item)
	{
		itemCount--;
		amount -= item.getAmount();
		itemList.remove(item.getLocalID());
	}
	
	public String getStatusString()
	{
		switch (status)
		{
			case 0:
				return "草稿";
			case 1:
				return "已提交";
			case 2:
				return "审批未通过";
			case 3:
				return "审批通过";
			default:
				break;
		}
		return "N/A";
	}
}
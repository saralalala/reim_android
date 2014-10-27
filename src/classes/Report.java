package classes;

import java.io.Serializable;
import java.util.List;

import database.DBManager;

public class Report implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final int STATUS_DRAFT = 0;
	public static final int STATUS_SUBMITTED = 1;
	public static final int STATUS_APPROVED = 2;
	public static final int STATUS_REJECTED = 3;
	public static final int STATUS_FINISHED = 4;
	
	private int localID = -1;
	private int serverID = -1;
	private String title = "";
	private int status = 0;
	private int managerID = -1;
	private User user = null;
	private int createdDate = -1;
	private int serverUpdatedDate = -1;
	private int localUpdatedDate = -1;
	
	public int getLocalID()
	{
		return localID;
	}
	public void setLocalID(int localID)
	{
		this.localID = localID;
	}
	
	public int getServerID()
	{
		return serverID;
	}
	public void setServerID(int serverID)
	{
		this.serverID = serverID;
	}
	
	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public int getStatus()
	{
		return status;
	}
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	public int getManagerID()
	{
		return managerID;
	}
	public void setManagerID(int managerID)
	{
		this.managerID = managerID;
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
	
	public String getStatusString()
	{
		switch (status)
		{
			case STATUS_DRAFT:
				return "草稿";
			case STATUS_SUBMITTED:
				return "已提交";
			case STATUS_APPROVED:
				return "审批通过";
			case STATUS_REJECTED:
				return "审批未通过";
			case STATUS_FINISHED:
				return "报销完成";
			default:
				return "N/A";
		}
	}
	
	public boolean hasItems()
	{
		List<Item> itemList = DBManager.getDBManager().getReportItems(localID);
		if (itemList.size() == 0)
		{
			return false;
		}
		return true;
	}

	public boolean canBeSubmitted()
	{
		List<Item> itemList = DBManager.getDBManager().getReportItems(localID);
		for (Item item : itemList)
		{
			if (item.getServerID() == -1 || (!item.getInvoicePath().equals("") && item.getImageID() == -1))
			{
				return false;
			}
		}
		return true;
	}
}
package classes;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.rushucloud.reim.R;

import android.util.SparseArray;
import android.util.SparseIntArray;

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
	private List<User> managerList = null;
	private List<User> ccList = null;
	private User user = null;
	private int createdDate = -1;
	private int serverUpdatedDate = -1;
	private int localUpdatedDate = -1;
	private int itemCount;
	private String amount;
	
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
	
	public List<User> getManagerList()
	{
		return managerList;
	}
	public String getManagersName()
	{
		if (getManagerList() == null || getManagerList().size() == 0)
		{
			return ReimApplication.getContext().getString(R.string.noManager);
		}
		else
		{
			return User.getUsersNameString(getManagerList());
		}
	}
	public void setManagerList(List<User> managerList)
	{
		this.managerList = managerList;
	}
	
	public List<User> getCCList()
	{
		return ccList;
	}
	public String getCCsName()
	{
		if (getCCList() == null || getCCList().size() == 0)
		{
			return ReimApplication.getContext().getString(R.string.noCC);
		}
		else
		{
			return User.getUsersNameString(getCCList());
		}
	}
	public void setCCList(List<User> ccList)
	{
		this.ccList = ccList;
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
	
	public int getItemCount()
	{
		return itemCount;
	}
	public void setItemCount(int count)
	{
		this.itemCount = count;
	}
	
	public String getAmount()
	{
		return amount;
	}
	public void setAmount(String amount)
	{
		this.amount = amount;
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
			if (!item.canBeSubmitWithReport())
			{
				return false;
			}
		}
		return true;
	}
    
	public boolean isInSpecificStatus(List<Integer> statusList)
	{
		for (Integer integer : statusList)
		{
			if (getStatus() == integer)
			{
				return true;
			}
		}
		return false;
	}
	
    public static void sortByItemsCount(List<Report> reportList)
    {
    	DBManager dbManager = DBManager.getDBManager();
    	final SparseIntArray countArray = new SparseIntArray();
    	for (Report report : reportList)
		{
    		int count = dbManager.getReportItemsCount(report.getLocalID());
			countArray.put(report.getLocalID(), count);
		}
    	
    	Collections.sort(reportList, new Comparator<Report>()
		{
			public int compare(Report report1, Report report2)
			{
				return (int)(countArray.get(report1.getLocalID()) - countArray.get(report2.getLocalID()));
			}
		});
    }
    
    public static void sortByAmount(List<Report> reportList)
    {
    	DBManager dbManager = DBManager.getDBManager();
    	final SparseArray<Double> countArray = new SparseArray<Double>();
    	for (Report report : reportList)
		{
    		double amount = dbManager.getReportAmount(report.getLocalID());
			countArray.put(report.getLocalID(), amount);
		}
    	
    	Collections.sort(reportList, new Comparator<Report>()
		{
			public int compare(Report report1, Report report2)
			{
				return (int)(countArray.get(report1.getLocalID()) - countArray.get(report2.getLocalID()));
			}
		});
    }
    
    public static void sortByCreateDate(List<Report> reportList)
    {
    	Collections.sort(reportList, new Comparator<Report>()
		{
			public int compare(Report report1, Report report2)
			{
				return (int)(report1.getCreatedDate() - report2.getCreatedDate());
			}
		});
    }
    
    public static void sortByModifyDate(List<Report> reportList)
    {
    	Collections.sort(reportList, new Comparator<Report>()
		{
			public int compare(Report report1, Report report2)
			{
				return (int)(report2.getLocalUpdatedDate() - report1.getLocalUpdatedDate());
			}
		});
    }
}
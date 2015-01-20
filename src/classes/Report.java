package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import classes.utils.DBManager;

import com.rushucloud.reim.R;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;


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
	private int status = Report.STATUS_DRAFT;
	private List<User> managerList = null;
	private List<User> ccList = null;
	private List<Comment> commentList = null;
	private User sender = null;
	private boolean isProveAhead = false;
	private int createdDate = -1;
	private int serverUpdatedDate = -1;
	private int localUpdatedDate = -1;
	private int itemCount;
	private String amount;
	private boolean isCC;
	
	public Report()
	{
		
	}
	
	public Report(Report report)
	{
		localID = report.getLocalID();
		serverID = report.getServerID();
		title = report.getTitle();
		status = report.getStatus();
		managerList = new ArrayList<User>(report.getManagerList());
		ccList = new ArrayList<User>(report.getCCList());
		commentList = new ArrayList<Comment>(report.getCommentList());
		sender = new User(report.getSender());
		isProveAhead = report.isProveAhead();
		createdDate = report.getCreatedDate();
		serverUpdatedDate = report.getServerUpdatedDate();
		localUpdatedDate = report.getLocalUpdatedDate();
		itemCount = report.getItemCount();
		amount = report.getAmount();
	}

	public Report(JSONObject jObject)
	{
		try
		{
			setServerID(jObject.getInt("id"));
			setTitle(jObject.getString("title"));
			setCreatedDate(jObject.getInt("createdt"));
			setStatus(jObject.getInt("status"));
			setLocalUpdatedDate(jObject.getInt("lastdt"));
			setServerUpdatedDate(jObject.getInt("lastdt"));
			
			User user = new User();
			user.setServerID(jObject.getInt("uid"));
			setSender(user);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
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
		if (getManagerList() == null || getManagerList().isEmpty())
		{
			return "";
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
		if (getCCList() == null || getCCList().isEmpty())
		{
			return "";
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

	public List<Comment> getCommentList()
	{
		return commentList;
	}
	public void setCommentList(List<Comment> commentList)
	{
		this.commentList = commentList;
	}
	
	public User getSender()
	{
		return sender;
	}
	public void setSender(User sender)
	{
		this.sender = sender;
	}

	public boolean isProveAhead()
	{
		return isProveAhead;
	}
	public void setIsProveAhead(boolean isProveAhead)
	{
		this.isProveAhead = isProveAhead;
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
		
	public boolean isCC()
	{
		return isCC;
	}
	public void setIsCC(boolean isCC)
	{
		this.isCC = isCC;
	}

	public int getStatusBackground()
    {
    	switch (getStatus())
		{
			case STATUS_DRAFT:
				return R.drawable.status_draft;
			case STATUS_SUBMITTED:
				return R.drawable.status_submitted;
			case STATUS_APPROVED:
				return R.drawable.status_approved;
			case STATUS_REJECTED:
				return R.drawable.status_rejected;
			case STATUS_FINISHED:
				return R.drawable.status_finished;
			default:
				return 0;
		}		
    }

	public int getStatusString()
	{
		switch (getStatus())
		{
			case STATUS_DRAFT:
				return R.string.status_draft;
			case STATUS_SUBMITTED:
				return R.string.status_submitted;
			case STATUS_APPROVED:
				return R.string.status_approved;
			case STATUS_REJECTED:
				return R.string.status_rejected;
			case STATUS_FINISHED:
				return R.string.status_finished;
			default:
				return R.string.not_available;
		}
	}

	public int getStatusWidth(Context context)
    {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		int longWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, metrics);
		int shortWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, metrics);
		
		switch (status)
		{
			case STATUS_DRAFT:
				return longWidth;
			case STATUS_SUBMITTED:
				return longWidth;
			case STATUS_APPROVED:
				return shortWidth;
			case STATUS_REJECTED:
				return shortWidth;
			case STATUS_FINISHED:
				return shortWidth;
			default:
				return 0;
		}
    }
	
	public boolean hasItems()
	{
		List<Item> itemList = DBManager.getDBManager().getReportItems(localID);
		if (itemList.isEmpty())
		{
			return false;
		}
		return true;
	}

	public boolean canBeSubmitted()
	{
		double amount = 0;
		List<Item> itemList = DBManager.getDBManager().getReportItems(localID);
		for (Item item : itemList)
		{
			if (!item.canBeSubmitWithReport())
			{
				return false;
			}
			amount += item.getAmount();
		}
		
		if (amount == 0)
		{
			return false;
		}
		
		return true;
	}
    
	public boolean isEditable()
	{
		return getStatus() == Report.STATUS_DRAFT || getStatus() == Report.STATUS_REJECTED;
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

	public static int getStatusString(int status)
	{
		switch (status)
		{
			case STATUS_DRAFT:
				return R.string.status_draft;
			case STATUS_SUBMITTED:
				return R.string.status_submitted;
			case STATUS_APPROVED:
				return R.string.status_approved;
			case STATUS_REJECTED:
				return R.string.status_rejected;
			case STATUS_FINISHED:
				return R.string.status_finished;
			default:
				return R.string.not_available;
		}
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
				return (int)(countArray.get(report2.getLocalID()) - countArray.get(report1.getLocalID()));
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
				return (int)(countArray.get(report2.getLocalID()) - countArray.get(report1.getLocalID()));
			}
		});
    }
    
    public static void sortByCreateDate(List<Report> reportList)
    {
    	Collections.sort(reportList, new Comparator<Report>()
		{
			public int compare(Report report1, Report report2)
			{
				return (int)(report2.getCreatedDate() - report1.getCreatedDate());
			}
		});
    }
    
    public static void sortByUpdateDate(List<Report> reportList)
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
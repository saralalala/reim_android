package classes;

import org.json.JSONException;
import org.json.JSONObject;

public class ApproveInfo implements Comparable<ApproveInfo>
{
	private int userID = -1;
	private int status = -1;
	private String approveTime = "";
	private String approveDate = "";
	
	public ApproveInfo()
	{
		
	}

	public ApproveInfo(JSONObject jObject)
	{
		try
		{
			setUserID(jObject.getInt("uid"));
			setStatus(jObject.getInt("status"));
			setApproveTime(jObject.getString("approvaldt").substring(11, 16));
			setApproveDate(jObject.getString("approvaldt").substring(0, 10));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}	
	}
	
	public int getUserID()
	{
		return userID;
	}

	public void setUserID(int userID)
	{
		this.userID = userID;
	}
	
	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public String getApproveTime()
	{
		return approveTime;
	}

	public void setApproveTime(String approveTime)
	{
		this.approveTime = approveTime;
	}

	public String getApproveDate()
	{
		return approveDate;
	}

	public void setApproveDate(String approveDate)
	{
		this.approveDate = approveDate;
	}

	public boolean hasApproved()
	{
		return status == Report.STATUS_APPROVED || status == Report.STATUS_REJECTED;
	}
	
	public int compareTo(ApproveInfo another)
	{
		return getStatus() - another.getStatus();
	}
}
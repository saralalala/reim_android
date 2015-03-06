package classes;

import org.json.JSONException;
import org.json.JSONObject;

public class ApproveInfo implements Comparable<ApproveInfo>
{
	private int userID = -1;
	private int status = -1;
	private String approveTime = "";
	private String approveDate = "";
	private int step = -1;
	
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
			setStep(jObject.getInt("step"));
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

	public int getStep()
	{
		return step;
	}

	public void setStep(int step)
	{
		this.step = step;
	}

	public boolean hasApproved()
	{
		return status == Report.STATUS_APPROVED || status == Report.STATUS_REJECTED;
	}
	
	public int compareTo(ApproveInfo another)
	{
		if (getStep() != another.getStep())
		{
			return getStep() - another.getStep();
		}
		else
		{
			return another.getStatus() - getStatus();
		}
	}
}
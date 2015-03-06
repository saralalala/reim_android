
package netUtils.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.utils.AppPreference;

public class EventsResponse extends BaseResponse
{
	private int reportEventCount;
	private int approveEventCount;
	private int inviteEventCount;
	private boolean needToRefresh;
	
	public EventsResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			JSONArray invitesArray = jObject.getJSONArray("invites");
			JSONArray reportsArray = jObject.getJSONArray("reports");	
			JSONArray membersArray = jObject.getJSONArray("members");
			JSONArray managersArray = jObject.getJSONArray("managers");
			
			reportEventCount = reportsArray.length();
			inviteEventCount = invitesArray.length();
			needToRefresh = (membersArray.length() + managersArray.length()) > 0;
			
			approveEventCount = 0;
			int currentUserID = AppPreference.getAppPreference().getCurrentUserID();
			for (int i = 0; i < reportEventCount; i++)
			{
				JSONObject object = reportsArray.getJSONObject(i);
				if (object.getInt("suid") != currentUserID)
				{
					approveEventCount++;
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public int getReportEventCount()
	{
		return reportEventCount;
	}

	public int getApproveEventCount()
	{
		return approveEventCount;
	}
	
	public int getInviteEventCount()
	{
		return inviteEventCount;
	}

	public boolean isNeedToRefresh()
	{
		return needToRefresh;
	}
}


package netUtils.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EventsResponse extends BaseResponse
{
	private int reportEventCount;
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

	public int getInviteEventCount()
	{
		return inviteEventCount;
	}

	public boolean isNeedToRefresh()
	{
		return needToRefresh;
	}
}

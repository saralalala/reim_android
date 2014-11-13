
package netUtils.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EventsResponse extends BaseResponse
{
	private int reportEventCount;
	private int meEventCount;
	
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
			meEventCount = invitesArray.length() + membersArray.length() + managersArray.length();
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

	public int getMeEventCount()
	{
		return meEventCount;
	}
}


package netUtils.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EventsResponse extends BaseResponse
{
	private JSONArray invitesArray;
	private JSONArray reportsArray;
	
	public EventsResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			invitesArray = jObject.getJSONArray("invites");
			reportsArray = jObject.getJSONArray("reports");			
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public JSONArray getInvitesArray()
	{
		return invitesArray;
	}

	public void setInvitesArray(JSONArray invitesArray)
	{
		this.invitesArray = invitesArray;
	}

	public JSONArray getReportsArray()
	{
		return reportsArray;
	}

	public void setReportsArray(JSONArray reportsArray)
	{
		this.reportsArray = reportsArray;
	}
}

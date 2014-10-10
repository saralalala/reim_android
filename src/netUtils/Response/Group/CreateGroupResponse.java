package netUtils.Response.Group;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.Response.BaseResponse;

public class CreateGroupResponse extends BaseResponse
{
	private int groupID;

	public CreateGroupResponse(Object httpResponse)
	{
		super(httpResponse);
		if (getStatus())
		{
			constructData();
		}
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			setGroupID(Integer.valueOf(jObject.getString("id")));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public int getGroupID()
	{
		return groupID;
	}

	public void setGroupID(int groupID)
	{
		this.groupID = groupID;
	}
}

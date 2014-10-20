package netUtils.Response.Group;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.Response.BaseResponse;

public class DeleteGroupResponse extends BaseResponse
{
	private int groupID;

	public DeleteGroupResponse(Object httpResponse)
	{
		super(httpResponse);
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

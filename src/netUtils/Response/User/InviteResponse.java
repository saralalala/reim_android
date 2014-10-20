package netUtils.Response.User;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.Response.BaseResponse;

public class InviteResponse extends BaseResponse
{
	private int userID;
	
	public InviteResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			setUserID(Integer.valueOf(jObject.getString("uid")));
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
}

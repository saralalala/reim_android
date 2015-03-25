package netUtils.response.user;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.response.BaseResponse;

public class RegisterResponse extends BaseResponse
{
	private int userID;
	
	public RegisterResponse(Object httpResponse)
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

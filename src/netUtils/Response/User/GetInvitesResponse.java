package netUtils.Response.User;

import org.json.JSONException;
import org.json.JSONObject;

import classes.User;

import netUtils.Response.BaseResponse;

public class GetInvitesResponse extends BaseResponse
{
	public GetInvitesResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			User user = new User();
			user.setEmail(jObject.getString("email"));
			user.setIsActive(Boolean.valueOf(jObject.getString("valid")));
			user.setDefaultManagerID(Integer.valueOf(jObject.getString("manager")));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}		
	}
}

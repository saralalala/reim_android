package netUtils.Response.User;

import org.json.JSONException;
import org.json.JSONObject;

import classes.User;

import netUtils.Response.BaseResponse;

public class UserInfoResponse extends BaseResponse
{
	private User user;
	
	public UserInfoResponse(Object httpResponse)
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

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}
}

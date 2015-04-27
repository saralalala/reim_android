package netUtils.response.user;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import classes.base.User;
import netUtils.response.BaseResponse;

public class SubordinatesInfoResponse extends BaseResponse
{
	private List<User> userList;
	
	private int total;
	
	public SubordinatesInfoResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			JSONArray jsonArray = jObject.getJSONArray("data");
			setTotal(Integer.valueOf(jObject.getString("total")));
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject object = jsonArray.getJSONObject(i);
				User user = new User();
				user.setEmail(object.getString("email"));
				user.setServerID(Integer.valueOf(object.getString("id")));
				userList.add(user);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public List<User> getUserList()
	{
		return userList;
	}

	public int getTotal()
	{
		return total;
	}

	public void setTotal(int total)
	{
		this.total = total;
	}
}
package netUtils.Response.Group;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.Group;
import classes.User;

import netUtils.Response.BaseResponse;

public class GetGroupResponse extends BaseResponse
{
	private Group group;
	private List<User> memberList;

	public GetGroupResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			
			int groupID = -1;
			JSONObject groupObject = jObject.getJSONObject("ginfo");
			if (groupObject.getInt("groupid") != -1)
			{
				group = new Group(groupObject);
				groupID = group.getServerID();
			}
			
			JSONArray memberArray = jObject.getJSONArray("gmember");
			memberList = new ArrayList<User>();
			for (int i = 0; i < memberArray.length(); i++)
			{
				User user = new User(memberArray.getJSONObject(i), groupID);
				memberList.add(user);
			}			
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	public Group getGroup()
	{
		return group;
	}

	public List<User> getMemberList()
	{
		return memberList;
	}
}

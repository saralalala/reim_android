package netUtils.Response.Group;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.Group;
import classes.User;
import classes.Utils;

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
				group = new Group();
				group.setServerID(groupObject.getInt("groupid"));
				group.setName(groupObject.getString("group_name"));
				group.setLocalUpdatedDate(groupObject.getInt("lastdt"));
				group.setServerUpdatedDate(groupObject.getInt("lastdt"));
				
				groupID = group.getServerID();
			}
			
			JSONArray memberArray = jObject.getJSONArray("gmember");
			memberList = new ArrayList<User>();
			for (int i = 0; i < memberArray.length(); i++)
			{
				JSONObject object = memberArray.getJSONObject(i);
				User user = new User();
				user.setServerID(Integer.valueOf(object.getString("id")));
				user.setEmail(object.getString("email"));
				user.setPhone(object.getString("phone"));
				user.setNickname(object.getString("nickname"));
				user.setIsAdmin(Utils.intToBoolean(object.getInt("admin")));
				user.setDefaultManagerID(object.getInt("manager_id"));
				user.setGroupID(groupID);
				user.setAvatarPath("");
				user.setLocalUpdatedDate(object.getInt("dt"));
				user.setServerUpdatedDate(object.getInt("dt"));
				String imageID = object.getString("avatar");
				if (imageID.equals(""))
				{
					user.setImageID(-1);					
				}
				else
				{
					user.setImageID(Integer.valueOf(imageID));
				}
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

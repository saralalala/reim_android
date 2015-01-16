
package netUtils.Response;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.Category;
import classes.Group;
import classes.Tag;
import classes.User;
import classes.utils.Utils;

public class CommonResponse extends BaseResponse
{
	private List<Category> categoryList;
	private List<Tag> tagList;
	private List<User> memberList;
	private User currentUser;
	private Group group;
	
	public CommonResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			
			JSONObject profileObject = jObject.getJSONObject("profile");
			
			int groupID = -1;
			JSONObject groupObject = profileObject.getJSONObject("group");
			if (groupObject.getInt("groupid") != -1)
			{
				group = new Group();
				group.setServerID(groupObject.getInt("groupid"));
				group.setName(groupObject.getString("group_name"));
				group.setLocalUpdatedDate(groupObject.getInt("lastdt"));
				group.setServerUpdatedDate(groupObject.getInt("lastdt"));
				
				groupID = group.getServerID();
			}

			currentUser = new User();
			currentUser.setServerID(profileObject.getInt("id"));
			currentUser.setNickname(profileObject.getString("nickname"));
			currentUser.setEmail(profileObject.getString("email"));
			currentUser.setPhone(profileObject.getString("phone"));
			currentUser.setDefaultManagerID(profileObject.getInt("manager_id"));
			currentUser.setAvatarPath("");
			currentUser.setIsAdmin(Utils.intToBoolean(profileObject.getInt("admin")));
			currentUser.setIsActive(Utils.intToBoolean(profileObject.getInt("active")));
			currentUser.setGroupID(groupID);
			currentUser.setLocalUpdatedDate(profileObject.getInt("lastdt"));
			currentUser.setServerUpdatedDate(profileObject.getInt("lastdt"));
			String imageID = profileObject.getString("avatar");
			if (imageID.equals(""))
			{
				currentUser.setAvatarID(-1);					
			}
			else
			{
				currentUser.setAvatarID(Integer.valueOf(imageID));
			}
			
			JSONArray categoryArray = jObject.getJSONArray("categories");
			categoryList = new ArrayList<Category>();
			for (int i = 0; i < categoryArray.length(); i++)
			{
				Category category =new Category(categoryArray.getJSONObject(i));
				categoryList.add(category);
			}
			
			JSONArray tagArray = jObject.getJSONArray("tags");
			tagList = new ArrayList<Tag>();
			for (int i = 0; i < tagArray.length(); i++)
			{
				Tag tag = new Tag(tagArray.getJSONObject(i));
				tagList.add(tag);
			}
			
			JSONArray memberArray = jObject.getJSONArray("members");
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
				imageID = object.getString("avatar");
				if (imageID.equals(""))
				{
					user.setAvatarID(-1);					
				}
				else
				{
					user.setAvatarID(Integer.valueOf(imageID));
				}
				memberList.add(user);
			}			
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public List<Category> getCategoryList()
	{
		return categoryList;
	}

	public List<Tag> getTagList()
	{
		return tagList;
	}

	public List<User> getMemberList()
	{
		return memberList;
	}

	public User getCurrentUser()
	{
		return this.currentUser;
	}

	public Group getGroup()
	{
		return group;
	}
}

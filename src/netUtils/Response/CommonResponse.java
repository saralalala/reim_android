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
import classes.Utils;

public class CommonResponse extends BaseResponse
{
	private List<Category> categoryList = null;
	private List<Tag> tagList = null;
	private List<User> memberList = null;
	private User currentUser = null;
	private Group group = null;
	public CommonResponse(Object httpResponse)
	{
		super(httpResponse);
		System.out.println("--------------  Call Init  -------------------");
		this.constructData();
	}

	@Override
	protected  void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			
			JSONObject profileObject = jObject.getJSONObject("profile");
			
			JSONObject groupObject = profileObject.getJSONObject("group");
			group = new Group();
			group.setId(groupObject.getInt("groupid"));
			group.setName(groupObject.getString("group_name"));
			group.setLocalUpdatedDate(groupObject.getInt("lastdt"));
			group.setServerUpdatedDate(groupObject.getInt("lastdt"));
			
			this.currentUser = new User();
			this.currentUser.setEmail(profileObject.getString("email"));
			this.currentUser.setNickname(profileObject.getString("nickname"));
			this.currentUser.setAvatarPath(profileObject.getString("path"));
			this.currentUser.setId(profileObject.getInt("id"));
			this.currentUser.setIsActive(profileObject.getInt("active") == 0 ? true : false);
			this.currentUser.setDefaultManagerID(profileObject.getInt("manager_id"));
			this.currentUser.setGroupID(group.getId());
			this.currentUser.setLocalUpdatedDate(Utils.getCurrentTime());
			this.currentUser.setServerUpdatedDate(Utils.getCurrentTime());
			
			JSONArray categoryArray = jObject.getJSONArray("categories");
			categoryList = new ArrayList<Category>();
			for (int i = 0; i < categoryArray.length(); i++)
			{
				JSONObject object = categoryArray.getJSONObject(i);
				Category category =new Category();
				category.setId(Integer.valueOf(object.getString("id")));
				category.setName(object.getString("category_name"));
				category.setLimit(Double.valueOf(object.getString("max_limit")));
				category.setGroupID(Integer.valueOf(object.getString("gid")));
				category.setParentID(Integer.valueOf(object.getString("pid")));
				category.setLocalUpdatedDate(object.getInt("lastdt"));
				category.setServerUpdatedDate(object.getInt("lastdt"));
				category.setIsProveAhead(object.getString("prove_before").equals("1") ? true : false);
				categoryList.add(category);
			}
			
			JSONArray tagArray = jObject.getJSONArray("tags");
			tagList = new ArrayList<Tag>();
			for (int i = 0; i < tagArray.length(); i++)
			{
				JSONObject object = tagArray.getJSONObject(i);
				Tag tag = new Tag();
				tag.setId(Integer.valueOf(object.getString("id")));
				tag.setName(object.getString("name"));
				tag.setGroupID(Integer.valueOf(object.getString("gid")));
				tag.setLocalUpdatedDate(object.getInt("lastdt"));
				tag.setServerUpdatedDate(object.getInt("lastdt"));
				tagList.add(tag);
			}
			
			JSONArray memberArray = jObject.getJSONArray("members");
			memberList = new ArrayList<User>();
			for (int i = 0; i < memberArray.length(); i++)
			{
				JSONObject object = memberArray.getJSONObject(i);
				User user = new User();
				user.setId(Integer.valueOf(object.getString("id")));
				user.setEmail(object.getString("email"));
				user.setPhone(object.getString("phone"));
				user.setNickname(object.getString("nickname"));
				user.setIsAdmin(object.getString("admin").equals("1") ? true : false);
				user.setDefaultManagerID(object.getInt("manager_id"));
				user.setGroupID(group.getId());
				user.setAvatarPath(object.getString("avatar"));
				user.setLocalUpdatedDate(Utils.getCurrentTime());
				user.setServerUpdatedDate(Utils.getCurrentTime());
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

package netUtils.Response;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.Category;
import classes.Tag;
import classes.User;

public class CommonResponse extends BaseResponse
{
	private List<Category> categoryList = null;
	private List<Tag> tagList = null;
	private List<User> memberList = null;
	private User currentUser = null;
	
	public CommonResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			
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
				category.setLastUpdatedDate(new Date(object.getLong("lastdt")));
				category.setPreBillable(Boolean.valueOf(object.getString("prove_before")));
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
				tag.setServerUpdatedDate(new Date(object.getLong("lastdt")));
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
				memberList.add(user);
			}						
			
			JSONObject object = jObject.getJSONObject("profile");
			currentUser = new User();
			currentUser.setEmail(object.getString("email"));
			currentUser.setPhone(object.getString("phone"));
			currentUser.setNickname(object.getString("nickname"));
			currentUser.setId(object.getInt("id"));
			currentUser.setGroupID(object.getInt("groupid"));
			currentUser.setIsActive(Boolean.valueOf(object.getString("active")));
			currentUser.setDefaultManagerID(object.getInt("manager_id"));
			//TODO set user's avatar
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
		return currentUser;
	}
}

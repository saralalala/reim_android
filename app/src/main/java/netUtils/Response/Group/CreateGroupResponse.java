package netUtils.response.group;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.Category;
import classes.Group;
import classes.Tag;
import classes.User;
import classes.utils.Utils;
import netUtils.response.BaseResponse;

public class CreateGroupResponse extends BaseResponse
{
	private int groupID;
    private int date;

    private List<Category> categoryList;
    private List<Tag> tagList;
    private List<User> memberList;
    private User currentUser;
    private Group group;

	public CreateGroupResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			setGroupID(Integer.valueOf(jObject.getString("id")));
            setDate(Integer.valueOf(jObject.getString("dt")));

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
            currentUser.parse(profileObject, groupID);

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
                User user = new User(memberArray.getJSONObject(i), groupID);
                memberList.add(user);
            }
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public int getGroupID()
	{
		return groupID;
	}

	public void setGroupID(int groupID)
	{
		this.groupID = groupID;
	}

    public int getDate()
    {
        return date;
    }

    public void setDate(int date)
    {
        this.date = date;
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

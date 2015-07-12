package netUtils.response.group;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Category;
import classes.model.Group;
import classes.model.SetOfBook;
import classes.model.Tag;
import classes.model.User;
import netUtils.response.common.BaseResponse;

public class CreateGroupResponse extends BaseResponse
{
    private int groupID;
    private int date;

    private List<SetOfBook> setOfBookList;
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
            groupID = jObject.getInteger("id");
            date = jObject.getInteger("dt");

            JSONObject profileObject = jObject.getJSONObject("profile");

            int groupID = -1;
            JSONObject groupObject = profileObject.getJSONObject("group");
            if (groupObject.getInteger("groupid") != -1)
            {
                group = new Group();
                group.setServerID(groupObject.getInteger("groupid"));
                group.setName(groupObject.getString("group_name"));
                group.setLocalUpdatedDate(groupObject.getInteger("lastdt"));
                group.setServerUpdatedDate(groupObject.getInteger("lastdt"));

                groupID = group.getServerID();
            }

            currentUser = new User();
            currentUser.parse(profileObject, groupID);

            JSONArray sobArray = profileObject.getJSONArray("sob");
            setOfBookList = new ArrayList<>();
            for (int i = 0; i < sobArray.size(); i++)
            {
                SetOfBook setOfBook = new SetOfBook(sobArray.getJSONObject(i), currentUser.getServerID());
                setOfBookList.add(setOfBook);
            }

            JSONArray categoryArray = jObject.getJSONArray("categories");
            categoryList = new ArrayList<>();
            for (int i = 0; i < categoryArray.size(); i++)
            {
                Category category = new Category(categoryArray.getJSONObject(i));
                categoryList.add(category);
            }

            JSONArray tagArray = jObject.getJSONArray("tags");
            tagList = new ArrayList<>();
            for (int i = 0; i < tagArray.size(); i++)
            {
                Tag tag = new Tag(tagArray.getJSONObject(i));
                tagList.add(tag);
            }

            JSONArray memberArray = jObject.getJSONArray("members");
            memberList = new ArrayList<>();
            for (int i = 0; i < memberArray.size(); i++)
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

    public int getDate()
    {
        return date;
    }

    public List<SetOfBook> getSetOfBookList()
    {
        return setOfBookList;
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

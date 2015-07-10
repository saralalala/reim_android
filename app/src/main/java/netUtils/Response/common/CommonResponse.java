package netUtils.response.common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Category;
import classes.model.Group;
import classes.model.SetOfBook;
import classes.model.Tag;
import classes.model.User;

public class CommonResponse extends BaseResponse
{
    private List<SetOfBook> setOfBookList;
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
            currentUser.parse(profileObject, groupID);

            JSONArray sobArray = profileObject.getJSONArray("sob");
            setOfBookList = new ArrayList<>();
            for (int i = 0; i < sobArray.length(); i++)
            {
                SetOfBook setOfBook = new SetOfBook(sobArray.getJSONObject(i), currentUser.getServerID());
                setOfBookList.add(setOfBook);
            }

            JSONArray categoryArray = jObject.getJSONArray("categories");
            categoryList = new ArrayList<>();
            for (int i = 0; i < categoryArray.length(); i++)
            {
                Category category = new Category(categoryArray.getJSONObject(i));
                categoryList.add(category);
            }

            JSONArray tagArray = jObject.getJSONArray("tags");
            tagList = new ArrayList<>();
            for (int i = 0; i < tagArray.length(); i++)
            {
                Tag tag = new Tag(tagArray.getJSONObject(i));
                tagList.add(tag);
            }

            JSONArray memberArray = jObject.getJSONArray("members");
            memberList = new ArrayList<>();
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

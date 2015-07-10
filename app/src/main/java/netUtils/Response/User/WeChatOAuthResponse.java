package netUtils.response.user;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.BankAccount;
import classes.model.Category;
import classes.model.Group;
import classes.model.SetOfBook;
import classes.model.Tag;
import classes.model.User;
import netUtils.response.common.BaseResponse;

public class WeChatOAuthResponse extends BaseResponse
{
    private List<SetOfBook> setOfBookList;
    private List<Category> categoryList;
    private List<Tag> tagList;
    private List<User> memberList;
    private User currentUser;
    private Group group;
    private int lastShownGuideVersion;

    public WeChatOAuthResponse(Object httpResponse)
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
                group = new Group(groupObject);
                groupID = group.getServerID();
            }

            currentUser = new User(profileObject, groupID);
            lastShownGuideVersion = profileObject.getInt("guide_version");

            JSONArray jsonArray = profileObject.getJSONArray("banks");
            if (jsonArray.length() > 0)
            {
                currentUser.setBankAccount(new BankAccount(jsonArray.getJSONObject(0)));
            }

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

    public int getLastShownGuideVersion()
    {
        return lastShownGuideVersion;
    }
}
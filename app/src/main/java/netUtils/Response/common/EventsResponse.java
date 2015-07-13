package netUtils.response.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.utils.AppPreference;
import classes.utils.LogUtils;
import classes.utils.Utils;

public class EventsResponse extends BaseResponse
{
    private List<Integer> mineUnreadList;
    private List<Integer> othersUnreadList;
    private int unreadMessagesCount;
    private boolean hasUnreadMessages;
    private boolean hasUnreadReports;
    private boolean needToRefresh;
    private String appliedCompany;
    private boolean currentUserActived;

    public EventsResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            LogUtils.println("Events:" + jObject.toString());
            JSONArray appliesArray = jObject.getJSONArray("applies");
            JSONArray invitesArray = jObject.getJSONArray("invites");
            JSONArray systemMessagesArray = jObject.getJSONArray("system");
            JSONArray adminMessagesArray = jObject.getJSONArray("questions");
            JSONArray reportsArray = jObject.getJSONArray("reports");
            JSONArray membersArray = jObject.getJSONArray("members");
            JSONArray managersArray = jObject.getJSONArray("managers");
            JSONArray categoriesArray = jObject.getJSONArray("categories");
            JSONArray tagsArray = jObject.getJSONArray("tags");
            boolean categoriesChanged = categoriesArray != null && categoriesArray.size() > 0;
            boolean tagsChanged = tagsArray != null && tagsArray.size() > 0;
            currentUserActived = Utils.intToBoolean(Utils.optInt(jObject, "active", 0));

            appliedCompany = jObject.getString("apply");

            int currentUserID = AppPreference.getAppPreference().getCurrentUserID();
            mineUnreadList = new ArrayList<>();
            othersUnreadList = new ArrayList<>();
            for (int i = 0; i < reportsArray.size(); i++)
            {
                JSONObject object = reportsArray.getJSONObject(i);
                if (object.getInteger("uid") == currentUserID)
                {
                    mineUnreadList.add(object.getInteger("fid"));
                }
                else
                {
                    othersUnreadList.add(object.getInteger("fid"));
                }
            }

            unreadMessagesCount = appliesArray.size() + invitesArray.size() + systemMessagesArray.size() + adminMessagesArray.size();
            hasUnreadMessages = unreadMessagesCount > 0;
            hasUnreadReports = reportsArray.size() > 0;

            boolean groupChanged = jObject.getInteger("gid") != AppPreference.getAppPreference().getCurrentGroupID();
            needToRefresh = groupChanged || (membersArray.size() + managersArray.size()) > 0 || categoriesChanged || tagsChanged;
        }   
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public List<Integer> getMineUnreadList()
    {
        return mineUnreadList;
    }

    public List<Integer> getOthersUnreadList()
    {
        return othersUnreadList;
    }

    public int getUnreadMessagesCount()
    {
        return unreadMessagesCount;
    }

    public boolean hasUnreadMessages()
    {
        return hasUnreadMessages;
    }

    public boolean hasUnreadReports()
    {
        return hasUnreadReports;
    }

    public boolean needToRefresh()
    {
        return needToRefresh;
    }

    public String getAppliedCompany()
    {
        return appliedCompany;
    }

    public boolean isCurrentUserActived()
    {
        return currentUserActived;
    }
}

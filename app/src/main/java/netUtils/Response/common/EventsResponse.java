package netUtils.response.common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.utils.AppPreference;

public class EventsResponse extends BaseResponse
{
    private List<Integer> mineUnreadList;
    private List<Integer> othersUnreadList;
    private int unreadMessagesCount;
    private boolean hasUnreadMessages;
    private boolean hasUnreadReports;
    private boolean needToRefresh;
    private String appliedCompany;

    public EventsResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            System.out.println("Events:" + jObject.toString());
            JSONArray appliesArray = jObject.getJSONArray("applies");
            JSONArray invitesArray = jObject.getJSONArray("invites");
            JSONArray systemMessagesArray = jObject.getJSONArray("system");
            JSONArray adminMessagesArray = jObject.getJSONArray("questions");
            JSONArray reportsArray = jObject.getJSONArray("reports");
            JSONArray membersArray = jObject.getJSONArray("members");
            JSONArray managersArray = jObject.getJSONArray("managers");
            JSONArray categoriesArray = jObject.getJSONArray("categories");
            JSONArray tagsArray = jObject.getJSONArray("tags");

            appliedCompany = jObject.getString("apply");

            int currentUserID = AppPreference.getAppPreference().getCurrentUserID();
            mineUnreadList = new ArrayList<>();
            othersUnreadList = new ArrayList<>();
            for (int i = 0; i < reportsArray.length(); i++)
            {
                JSONObject object = reportsArray.getJSONObject(i);
                if (object.getInt("uid") == currentUserID)
                {
                    mineUnreadList.add(object.getInt("fid"));
                }
                else
                {
                    othersUnreadList.add(object.getInt("fid"));
                }
            }

            unreadMessagesCount = appliesArray.length() + invitesArray.length() + systemMessagesArray.length() + adminMessagesArray.length();
            hasUnreadMessages = unreadMessagesCount > 0;
            hasUnreadReports = reportsArray.length() > 0;

            boolean groupChanged = jObject.getInt("gid") != AppPreference.getAppPreference().getCurrentGroupID();
            needToRefresh = groupChanged || (membersArray.length() + managersArray.length()) > 0 || categoriesArray.length() > 0 || tagsArray.length() > 0;
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
}

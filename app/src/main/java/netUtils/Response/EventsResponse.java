
package netUtils.response;

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
    private boolean hasUnreadReports;
	private boolean needToRefresh;
	
	public EventsResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
            System.out.println(jObject.toString());
			JSONArray invitesArray = jObject.getJSONArray("invites");
            JSONArray systemMessagesArray = jObject.getJSONArray("system");
            JSONArray adminMessagesArray = jObject.getJSONArray("questions");
			JSONArray reportsArray = jObject.getJSONArray("reports");
			JSONArray membersArray = jObject.getJSONArray("members");
			JSONArray managersArray = jObject.getJSONArray("managers");

            int currentUserID = AppPreference.getAppPreference().getCurrentUserID();
            mineUnreadList = new ArrayList<Integer>();
            othersUnreadList = new ArrayList<Integer>();
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

            unreadMessagesCount = invitesArray.length() + systemMessagesArray.length() + adminMessagesArray.length();
            hasUnreadReports = reportsArray.length() > 0;
			needToRefresh = (membersArray.length() + managersArray.length()) > 0;
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

    public boolean hasUnreadReports()
    {
        return hasUnreadReports;
    }

	public boolean needToRefresh()
	{
		return needToRefresh;
	}
}

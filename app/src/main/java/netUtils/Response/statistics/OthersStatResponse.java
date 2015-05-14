package netUtils.response.statistics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.StatCategory;
import classes.model.StatTag;
import classes.model.StatUser;
import netUtils.response.BaseResponse;

public class OthersStatResponse extends BaseResponse
{
	private List<StatCategory> statCategoryList;
    private List<StatTag> statTagList;
    private List<StatUser> statUserList;

	public OthersStatResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			
			JSONArray categories = jObject.getJSONArray("categories");
			this.statCategoryList = new ArrayList<StatCategory>();
			for (int i = 0; i < categories.length(); i++)
			{
				JSONObject object = categories.getJSONObject(i);
				this.statCategoryList.add(new StatCategory(object));
			}

            JSONArray tags = jObject.getJSONArray("tags");
            this.statTagList = new ArrayList<StatTag>();
            for (int i = 0; i < tags.length(); i++)
            {
                JSONObject object = tags.getJSONObject(i);
                this.statTagList.add(new StatTag(object));
            }

            JSONArray members = jObject.getJSONArray("members");
            this.statUserList = new ArrayList<StatUser>();
            for (int i = 0; i < members.length(); i++)
            {
                JSONObject object = members.getJSONObject(i);
                this.statUserList.add(new StatUser(object));
            }

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public List<StatCategory> getStatCategoryList()
	{
		return statCategoryList;
	}

    public List<StatTag> getStatTagList()
    {
        return statTagList;
    }

    public List<StatUser> getStatUserList()
    {
        return statUserList;
    }
}
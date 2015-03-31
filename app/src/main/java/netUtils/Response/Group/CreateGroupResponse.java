package netUtils.response.group;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.response.BaseResponse;

public class CreateGroupResponse extends BaseResponse
{
	private int groupID;
    private int date;

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
}

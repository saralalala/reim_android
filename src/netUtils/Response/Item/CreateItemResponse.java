package netUtils.Response.Item;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.Response.BaseResponse;

public class CreateItemResponse extends BaseResponse
{
	private int itemID;
	
	public CreateItemResponse(Object httpResponse)
	{
		super(httpResponse);
		if (getStatus())
		{
			constructData();
		}
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			setItemID(Integer.valueOf(jObject.getString("id")));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public int getItemID()
	{
		return itemID;
	}

	public void setItemID(int itemID)
	{
		this.itemID = itemID;
	}
}

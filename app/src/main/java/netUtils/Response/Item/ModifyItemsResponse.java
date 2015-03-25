package netUtils.response.item;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.response.BaseResponse;

public class ModifyItemsResponse extends BaseResponse
{
	private int itemID;
	
	public ModifyItemsResponse(Object httpResponse)
	{
		super(httpResponse);
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

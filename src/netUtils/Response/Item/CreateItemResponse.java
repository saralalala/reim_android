package netUtils.Response.Item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.Utils;

import netUtils.Response.BaseResponse;

public class CreateItemResponse extends BaseResponse
{
	private int itemID;
	
	public CreateItemResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONArray jsonArray = getDataArray();
			JSONObject jObject = jsonArray.getJSONObject(0);
			setStatus(Utils.intToBoolean(jObject.getInt("status")));
			if (getStatus())
			{
				setItemID(Integer.valueOf(jObject.getString("iid")));				
			}
			else
			{
				setErrorMessage(jObject.getString("msg"));				
			}
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

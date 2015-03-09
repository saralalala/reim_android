package netUtils.Response.Item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.utils.Utils;
import netUtils.Response.BaseResponse;

public class CreateItemResponse extends BaseResponse
{
	private int itemID;
	private int createDate;
	
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
				itemID = jObject.getInt("iid");
				setCreateDate(jObject.getInt("createdt"));
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

	public int getCreateDate()
	{
		return createDate;
	}

	public void setCreateDate(int createDate)
	{
		this.createDate = createDate;
	}
}

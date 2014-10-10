package netUtils.Response.Item;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.Category;
import classes.Item;
import classes.User;
import classes.Utils;

import netUtils.Response.BaseResponse;

public class GetItemsResponse extends BaseResponse
{
	List<Item> itemList = null;
	public GetItemsResponse(Object httpResponse)
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
			
			itemList = new ArrayList<Item>();
			JSONArray jsonArray = jObject.getJSONArray("items");
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject object = jsonArray.getJSONObject(i);
				Item item = new Item();
				item.setId(object.getInt("id"));
				item.setAmount(object.getDouble("amount"));
				item.setMerchant(object.getString("merchants"));
				item.setNote(object.getString("note"));
				item.setConsumedDate(object.getInt("dt"));
				item.setServerUpdatedDate(object.getInt("lastdt"));
				
//				item.setLocalUpdatedDate();
				//TODO Check local time
				
				item.setImageID(object.getInt("image_id"));
				//TODO get image from server
				
				Category category = new Category();
				category.setId(object.getInt("category"));
				//TODO request for common once again
				item.setCategory(category);
				
				User user = new User();
				user.setId(object.getInt("uid"));
				//TODO get user
				item.setConsumer(user);

				item.setIsProveAhead(Utils.intToBoolean(object.getInt("billable")));
				
				itemList.add(item);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
}

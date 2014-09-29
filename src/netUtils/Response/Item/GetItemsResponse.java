package netUtils.Response.Item;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.Category;
import classes.Item;
import classes.User;

import netUtils.Response.BaseResponse;

public class GetItemsResponse extends BaseResponse
{
	List<Item> itemList = null;
	public GetItemsResponse(Object httpResponse)
	{
		super(httpResponse);
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
				item.setConsumedDate(new Date(object.getLong("dt")));
				item.setServerUpdatedDate(new Date(object.getLong("lastdt")));
				
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
				
				if (object.getInt("billable") > 0)
				{
					item.setBillable(true);
				}
				else
				{
					item.setBillable(false);
				}
				
				itemList.add(item);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
}

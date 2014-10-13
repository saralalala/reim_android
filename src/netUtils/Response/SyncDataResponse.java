package netUtils.Response;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.Category;
import classes.Item;
import classes.Report;
import classes.User;
import classes.Utils;

public class SyncDataResponse extends BaseResponse
{
	private List<Item> itemList;
	
	private List<Report> reportList;
	
	public SyncDataResponse(Object httpResponse)
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
				item.setServerID(object.getInt("id"));
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
				category.setServerID(object.getInt("category"));
				//TODO request for common once again
				item.setCategory(category);
				
				User user = new User();
				user.setServerID(object.getInt("uid"));
				//TODO get user
				item.setConsumer(user);

				item.setIsProveAhead(Utils.intToBoolean(object.getInt("billable")));
				
				itemList.add(item);
			}
			
			reportList = new ArrayList<Report>();
			jsonArray = jObject.getJSONArray("reports");
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject object = jsonArray.getJSONObject(i);
				Report report = new Report();
				report.setServerID(object.getInt("id"));
				report.setTitle(object.getString("title"));
				report.setCreatedDate(object.getInt("createdt"));
				report.setServerUpdatedDate(object.getInt("lastdt"));
				report.setStatus(object.getInt("status"));
				
				User user = new User();
				user.setServerID(object.getInt("uid"));
				// TODO get user information
				
				report.setUser(user);
				
				reportList.add(report);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public List<Item> getItemList()
	{
		return itemList;
	}

	public List<Report> getReportList()
	{
		return reportList;
	}
}

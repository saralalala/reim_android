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
	private List<Item> itemList = null;
	
	private List<Report> reportList = null;
	
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
			
			reportList = new ArrayList<Report>();
			JSONArray jsonArray = jObject.getJSONArray("reports");
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject object = jsonArray.getJSONObject(i);
				Report report = new Report();
				report.setServerID(object.getInt("id"));
				report.setTitle(object.getString("title"));
				report.setCreatedDate(object.getInt("createdt"));
				report.setLocalUpdatedDate(object.getInt("lastdt"));
				report.setServerUpdatedDate(object.getInt("lastdt"));
				report.setStatus(object.getInt("status"));
				
				User user = new User();
				user.setServerID(object.getInt("uid"));
				report.setUser(user);
				
				reportList.add(report);
			}
			
			itemList = new ArrayList<Item>();
			jsonArray = jObject.getJSONArray("items");
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
				item.setLocalUpdatedDate(object.getInt("lastdt"));				
				item.setImageID(object.getInt("image_id"));		
				item.setInvoicePath("");
				item.setIsProveAhead(Utils.intToBoolean(object.getInt("prove_ahead")));
				item.setNeedReimbursed(Utils.intToBoolean(object.getInt("reimbursed")));
				
				Report report = new Report();
				report.setServerID(object.getInt("rid"));
				item.setBelongReport(report);				
				
				Category category = new Category();
				category.setServerID(object.getInt("category"));
				item.setCategory(category);
				
				User user = new User();
				user.setServerID(object.getInt("uid"));
				item.setConsumer(user);
				
				itemList.add(item);
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

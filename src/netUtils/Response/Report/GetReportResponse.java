package netUtils.Response.Report;

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
import database.DBManager;
import netUtils.Response.BaseResponse;

public class GetReportResponse extends BaseResponse
{
	private Report report;
	private List<Item> itemList;
	
	public GetReportResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			report = new Report();
			report.setServerID(jObject.getInt("rid"));
			report.setTitle(jObject.getString("title"));
			report.setCreatedDate(jObject.getInt("createdt"));
			report.setStatus(jObject.getInt("status"));
			report.setLocalUpdatedDate(jObject.getInt("lastdt"));
			report.setServerUpdatedDate(jObject.getInt("lastdt"));

			User user = new User();
			user.setServerID(jObject.getInt("uid"));
			report.setUser(user);
			
			DBManager dbManager = DBManager.getDBManager();

			JSONObject receiverObject = jObject.getJSONObject("receivers");
			
			JSONArray managerArray = receiverObject.getJSONArray("managers");
			List<User> managerList = new ArrayList<User>();
			for (int i = 0; i < managerArray.length(); i++)
			{
				JSONObject object = managerArray.getJSONObject(i);
				managerList.add(dbManager.getUser(object.getInt("id")));
			}
			report.setManagerList(managerList);
			
			JSONArray ccArray = receiverObject.getJSONArray("cc");
			List<User> ccList = new ArrayList<User>();
			for (int i = 0; i < ccArray.length(); i++)
			{
				JSONObject object = ccArray.getJSONObject(i);
				ccList.add(dbManager.getUser(object.getInt("id")));
			}
			report.setCCList(ccList);
			
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
				item.setLocalUpdatedDate(object.getInt("lastdt"));				
				item.setImageID(object.getInt("image_id"));		
				item.setInvoicePath("");
				item.setIsProveAhead(Utils.intToBoolean(object.getInt("prove_ahead")));
				item.setNeedReimbursed(Utils.intToBoolean(object.getInt("reimbursed")));
				item.setTagsID(object.getString("tags"));
				item.setRelevantUsersID(object.getString("relates"));
				
				Report report = new Report();
				report.setServerID(object.getInt("rid"));
				item.setBelongReport(report);				
				
				Category category = new Category();
				category.setServerID(object.getInt("category"));
				item.setCategory(category);
				
				User itemUser = new User();
				itemUser.setServerID(object.getInt("uid"));
				item.setConsumer(itemUser);
				
				itemList.add(item);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public Report getReport()
	{
		return report;
	}

	public void setReport(Report report)
	{
		this.report = report;
	}

	public List<Item> getItemList()
	{
		return itemList;
	}

	public void setItemList(List<Item> itemList)
	{
		this.itemList = itemList;
	}
}

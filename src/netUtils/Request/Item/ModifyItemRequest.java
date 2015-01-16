package netUtils.Request.Item;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import classes.Image;
import classes.Item;
import classes.Tag;
import classes.User;
import classes.utils.Utils;
import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class ModifyItemRequest extends BaseRequest
{
	public ModifyItemRequest(Item item)
	{
		super();
		
		try
		{
			JSONArray jsonArray = new JSONArray();

			int categoryID = item.getCategory() == null ? -1 : item.getCategory().getServerID();
			JSONObject jObject = new JSONObject();
			jObject.put("id", Integer.toString(item.getServerID()));
			jObject.put("amount", Utils.formatDouble(item.getAmount()));
			jObject.put("category", categoryID);
			jObject.put("merchants", item.getVendor());
			jObject.put("location", item.getLocation());
			jObject.put("uid", item.getConsumer().getServerID());
			jObject.put("prove_ahead", Utils.booleanToString(item.isProveAhead()));
			jObject.put("image_id", Image.getImagesIDString(item.getInvoices()));
			jObject.put("uids", User.getUsersIDString(item.getRelevantUsers()));
			jObject.put("tags", Tag.getTagsIDString(item.getTags()));
			jObject.put("dt", item.getConsumedDate());
			jObject.put("note", item.getNote());
			jObject.put("reimbursed", Utils.booleanToString(item.needReimbursed()));
			jObject.put("local_id", item.getLocalID());
			
			jsonArray.put(jObject);
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("items", jsonArray.toString()));
			setParams(params);

			appendUrl("/item");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

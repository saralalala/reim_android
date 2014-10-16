package netUtils.Request.Item;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import classes.Item;
import classes.Utils;
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
			
			String uids = "";
			if (item.getRelevantUsers() != null)
			{
				int count = item.getRelevantUsers().size();
				for (int i = 0; i < count; i++)
				{
					uids += item.getRelevantUsers().get(i).getServerID() + ",";
				}
				if (uids.length() > 0)
				{
					uids = uids.substring(0, uids.length()-1);			
				}			
			}
			
			String tags = "";
			if (item.getTags() != null)
			{
				int count = item.getTags().size();
				for (int i = 0; i < count; i++)
				{
					tags += item.getTags().get(i).getServerID() + ",";
				}
				if (tags.length() > 0)
				{
					tags = tags.substring(0, tags.length()-1);			
				}			
			}
			
			JSONObject jObject = new JSONObject();
			jObject.put("id", Integer.toString(item.getServerID()));
			jObject.put("amount", item.getAmount());
			jObject.put("category", item.getCategory().getServerID());
			jObject.put("merchants", item.getMerchant());
			jObject.put("uid", item.getConsumer().getServerID());
			jObject.put("prove_ahead", Utils.booleanToString(item.isProveAhead()));
			jObject.put("image_id", item.getImageID());
			jObject.put("uids", uids);
			jObject.put("tags", tags);
			jObject.put("dt", item.getConsumedDate());
			jObject.put("note", item.getNote());
			jObject.put("reimbursed", Utils.booleanToString(item.needReimbursed()));
			jObject.put("local_id", item.getLocalID());
			
			jsonArray.put(jObject);
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("items", jsonArray.toString()));
			setParams(params);

			String requestUrl = getUrl();
			requestUrl += "/item";
			setUrl(requestUrl);
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

package netUtils.Request.Item;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import classes.Item;
import netUtils.Request.BaseRequest;

public class ModifyItemRequest extends BaseRequest
{
	public ModifyItemRequest(Item item)
	{
		super();
		
		String uids = "";
		int count = item.getRelevantUsers().size();
		for (int i = 0; i < count; i++)
		{
			uids += item.getRelevantUsers().get(i).getId() + ",";
		}
		if (uids.length() > 0)
		{
			uids = uids.substring(0, uids.length()-1);			
		}
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("amount", Double.toString(item.getAmount())));
		params.add(new BasicNameValuePair("category", Integer.toString(item.getCategory().getId())));
		params.add(new BasicNameValuePair("merchants", item.getMerchant()));
		params.add(new BasicNameValuePair("billable", Boolean.toString(item.getBillable())));
		params.add(new BasicNameValuePair("image_id", Integer.toString(item.getImageID())));
		params.add(new BasicNameValuePair("uids", uids));
		params.add(new BasicNameValuePair("dt", Integer.toString(item.getConsumedDate())));
		setParams(params);

		String requestUrl = getUrl();
		requestUrl += "/item/" + item.getId();
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}

}

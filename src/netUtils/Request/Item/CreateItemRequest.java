package netUtils.Request.Item;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import classes.Item;

import netUtils.Request.BaseRequest;

public class CreateItemRequest extends BaseRequest
{
	public CreateItemRequest(Item item)
	{
		super();
		
		String uids = "";
		int count = item.getRelevantUsers().size();
		for (int i = 0; i < count; i++)
		{
			uids += item.getRelevantUsers().get(i).getServerID() + ",";
		}
		if (uids.length() > 0)
		{
			uids = uids.substring(0, uids.length()-1);			
		}
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("amount", Double.toString(item.getAmount())));
		params.add(new BasicNameValuePair("category", Integer.toString(item.getCategory().getServerID())));
		params.add(new BasicNameValuePair("merchants", item.getMerchant()));
		params.add(new BasicNameValuePair("billable", Boolean.toString(item.isProveAhead())));
		params.add(new BasicNameValuePair("image_id", Integer.toString(item.getImageID())));
		params.add(new BasicNameValuePair("uids", uids));
		params.add(new BasicNameValuePair("dt", Integer.toString(item.getConsumedDate())));
		setParams(params);

		String requestUrl = getUrl();
		requestUrl += "/item";
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}

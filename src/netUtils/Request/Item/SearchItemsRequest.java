package netUtils.Request.Item;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class SearchItemsRequest extends BaseRequest
{
	public SearchItemsRequest(String keyword)
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("keyword", keyword));
		setParams(params);
		
		String requestUrl = getUrl();
		requestUrl += "/search";
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}

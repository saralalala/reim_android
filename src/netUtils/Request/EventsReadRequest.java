package netUtils.Request;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;

public class EventsReadRequest extends BaseRequest
{
	public EventsReadRequest(int type)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("type", Integer.toString(type)));
		setParams(params);

		String requestUrl = getUrl();
		requestUrl += "/events";
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}
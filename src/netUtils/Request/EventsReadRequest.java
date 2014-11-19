package netUtils.Request;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;

public class EventsReadRequest extends BaseRequest
{
	public final static int TYPE_REPORT = 0;
	public final static int TYPE_INVITE = 1;
	public final static int TYPE_MANAGER = 2;
	public final static int TYPE_MEMBER = 3;
	
	public EventsReadRequest(int type)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("type", Integer.toString(type)));
		setParams(params);

		appendUrl("/events");
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}
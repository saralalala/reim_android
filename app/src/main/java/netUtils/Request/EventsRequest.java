package netUtils.request;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;

public class EventsRequest extends BaseRequest
{
	public EventsRequest()
	{
		super();

		appendUrl(URLDef.URL_EVENT);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
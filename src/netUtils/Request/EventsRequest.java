package netUtils.Request;

import netUtils.HttpConnectionCallback;

public class EventsRequest extends BaseRequest
{
	public EventsRequest()
	{
		super();

		appendUrl("/events");
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
package netUtils.Request;

import netUtils.HttpConnectionCallback;

public class EventsRequest extends BaseRequest
{
	public EventsRequest()
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/events";
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}
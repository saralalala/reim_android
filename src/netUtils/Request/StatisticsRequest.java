package netUtils.Request;

import netUtils.HttpConnectionCallback;

public class StatisticsRequest extends BaseRequest
{

	public StatisticsRequest()
	{
		super();
		String requestUrl = getUrl();
		requestUrl += "/stat";
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

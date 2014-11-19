package netUtils.Request;

import netUtils.HttpConnectionCallback;

public class StatisticsRequest extends BaseRequest
{

	public StatisticsRequest()
	{
		super();

		appendUrl("/stat");
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

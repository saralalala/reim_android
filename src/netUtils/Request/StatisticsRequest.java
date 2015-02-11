package netUtils.Request;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;

public class StatisticsRequest extends BaseRequest
{

	public StatisticsRequest()
	{
		super();

		appendUrl(URLDef.URL_STATISTICS);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

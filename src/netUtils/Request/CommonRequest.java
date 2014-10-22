package netUtils.Request;

import netUtils.HttpConnectionCallback;

public class CommonRequest extends BaseRequest
{
	public CommonRequest()
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/common/0";
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
package netUtils.Request;

import netUtils.HttpConnectionCallback;

public class CommonRequest extends BaseRequest
{
	public CommonRequest()
	{
		super();

		appendUrl("/common/0");
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
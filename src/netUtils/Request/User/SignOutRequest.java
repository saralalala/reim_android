package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class SignOutRequest extends BaseRequest
{
	public SignOutRequest()
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/logout";
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

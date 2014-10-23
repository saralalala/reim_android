package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class SignInRequest extends BaseRequest
{
	public SignInRequest()
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/login";
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
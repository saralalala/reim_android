package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

public class SignInRequest extends BaseRequest
{
	public SignInRequest()
	{
		super();

		appendUrl(URLDef.URL_SIGN_IN);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
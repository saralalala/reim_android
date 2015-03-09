package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import netUtils.URLDef;

public class SignInRequest extends BaseRequest
{
	public SignInRequest()
	{
		super(5, 2);

		appendUrl(URLDef.URL_SIGN_IN);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
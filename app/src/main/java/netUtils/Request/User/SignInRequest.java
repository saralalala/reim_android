package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

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
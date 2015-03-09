package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import netUtils.URLDef;

public class SignOutRequest extends BaseRequest
{
	public SignOutRequest()
	{
		super();

		appendUrl(URLDef.URL_SIGN_OUT);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

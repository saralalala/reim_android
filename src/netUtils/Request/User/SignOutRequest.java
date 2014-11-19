package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class SignOutRequest extends BaseRequest
{
	public SignOutRequest()
	{
		super();

		appendUrl("/logout");
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

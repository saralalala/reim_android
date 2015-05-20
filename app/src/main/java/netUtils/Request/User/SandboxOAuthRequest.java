package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class SandboxOAuthRequest extends BaseRequest
{
	public SandboxOAuthRequest()
	{
		super();
		
		appendUrl(URLDef.URL_OAUTH);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}
package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

public class GetInvitesRequest extends BaseRequest
{
	public GetInvitesRequest()
	{
		super();

		appendUrl(URLDef.URL_INVITE_LIST);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class GetInvitesRequest extends BaseRequest
{
	public GetInvitesRequest()
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
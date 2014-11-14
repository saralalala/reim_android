package netUtils.Request.Group;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class GetGroupRequest extends BaseRequest
{
	public GetGroupRequest()
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/groups";
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
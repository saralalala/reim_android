package netUtils.Request.Group;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class DeleteGroupRequest extends BaseRequest
{
	public DeleteGroupRequest()
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/groups";
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}
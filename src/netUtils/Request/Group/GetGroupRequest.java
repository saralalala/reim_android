package netUtils.Request.Group;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class GetGroupRequest extends BaseRequest
{
	public GetGroupRequest()
	{
		super();

		appendUrl("/groups");
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
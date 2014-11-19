package netUtils.Request.Group;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class DeleteGroupRequest extends BaseRequest
{
	public DeleteGroupRequest()
	{
		super();

		appendUrl("/groups");
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}
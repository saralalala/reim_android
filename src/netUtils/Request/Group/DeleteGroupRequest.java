package netUtils.Request.Group;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

public class DeleteGroupRequest extends BaseRequest
{
	public DeleteGroupRequest()
	{
		super();

		appendUrl(URLDef.URL_GROUP);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}
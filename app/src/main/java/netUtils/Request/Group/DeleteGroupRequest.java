package netUtils.request.group;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

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
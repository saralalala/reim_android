package netUtils.Request.Group;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import netUtils.URLDef;

public class GetGroupRequest extends BaseRequest
{
	public GetGroupRequest()
	{
		super();

		appendUrl(URLDef.URL_GROUP);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
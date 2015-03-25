package netUtils.request.group;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

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
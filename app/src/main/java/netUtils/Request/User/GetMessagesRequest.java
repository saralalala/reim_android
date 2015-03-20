package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import netUtils.URLDef;

public class GetMessagesRequest extends BaseRequest
{
	public GetMessagesRequest()
	{
		super();

		appendUrl(URLDef.URL_MESSAGE_LIST);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
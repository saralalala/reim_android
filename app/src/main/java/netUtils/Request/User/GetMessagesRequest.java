package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

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
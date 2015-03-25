package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class GetMessageRequest extends BaseRequest
{
	public GetMessageRequest(int type, int messageID)
	{
		super();

		appendUrl(URLDef.URL_MESSAGE + "/" + type + "/" + messageID);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
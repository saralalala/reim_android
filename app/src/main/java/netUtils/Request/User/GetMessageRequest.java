package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import netUtils.URLDef;

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
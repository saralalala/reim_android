package netUtils.request.tag;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class DeleteTagRequest extends BaseRequest
{
	public DeleteTagRequest(int tagID)
	{
		super();

		appendUrl(URLDef.URL_TAG + "/" + tagID);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

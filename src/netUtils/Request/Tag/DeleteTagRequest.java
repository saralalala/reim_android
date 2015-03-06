package netUtils.Request.Tag;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

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

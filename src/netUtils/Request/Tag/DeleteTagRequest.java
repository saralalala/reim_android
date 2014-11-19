package netUtils.Request.Tag;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class DeleteTagRequest extends BaseRequest
{
	public DeleteTagRequest(int tagID)
	{
		super();

		String urlSuffix = "/tags/" + tagID;
		appendUrl(urlSuffix);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

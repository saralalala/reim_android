package netUtils.Request.Tag;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class DeleteTagRequest extends BaseRequest
{
	public DeleteTagRequest(int tagID)
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/tags/" + tagID;
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

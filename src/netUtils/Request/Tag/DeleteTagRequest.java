package netUtils.Request.Tag;

import classes.Tag;
import netUtils.Request.BaseRequest;

public class DeleteTagRequest extends BaseRequest
{
	public DeleteTagRequest(Tag tag)
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/tags/" + tag.getId();
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}

}

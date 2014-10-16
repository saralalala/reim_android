package netUtils.Request.Category;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class DeleteCategoryRequest extends BaseRequest
{
	public DeleteCategoryRequest(int categoryID)
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/category/" + categoryID;
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

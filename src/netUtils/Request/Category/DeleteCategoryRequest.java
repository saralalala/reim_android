package netUtils.Request.Category;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class DeleteCategoryRequest extends BaseRequest
{
	public DeleteCategoryRequest(int categoryID)
	{
		super();

		String urlSuffix = "/category/" + categoryID;
		appendUrl(urlSuffix);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

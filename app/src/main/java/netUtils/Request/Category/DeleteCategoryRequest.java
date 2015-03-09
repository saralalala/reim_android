package netUtils.Request.Category;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import netUtils.URLDef;

public class DeleteCategoryRequest extends BaseRequest
{
	public DeleteCategoryRequest(int categoryID)
	{
		super();

		appendUrl(URLDef.URL_CATEGORY + "/" + categoryID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

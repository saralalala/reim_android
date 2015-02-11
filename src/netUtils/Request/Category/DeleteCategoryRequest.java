package netUtils.Request.Category;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

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

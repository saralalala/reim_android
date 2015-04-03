package netUtils.request.category;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class DeleteCategoryRequest extends BaseRequest
{
	public DeleteCategoryRequest(int categoryID)
	{
		super();

		appendUrl(URLDef.URL_CATEGORY);
        appendUrl(categoryID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

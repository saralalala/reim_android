package netUtils.Request.Category;

import classes.Category;
import netUtils.Request.BaseRequest;

public class DeleteCategoryRequest extends BaseRequest
{
	public DeleteCategoryRequest(Category category)
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/category/" + category.getId();
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

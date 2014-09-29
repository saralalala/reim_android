package netUtils.Request.Item;

import classes.Item;
import netUtils.Request.BaseRequest;

public class DeleteItemRequest extends BaseRequest
{
	public DeleteItemRequest(Item item)
	{
		super();
		
		String requestUrl = getUrl();
		requestUrl += "/item/" + item.getId();
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

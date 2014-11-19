package netUtils.Request.Item;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class DeleteItemRequest extends BaseRequest
{
	public DeleteItemRequest(int itemID)
	{
		super();
		
		String urlSuffix = "/item/" + itemID;
		appendUrl(urlSuffix);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

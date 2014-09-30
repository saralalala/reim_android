package netUtils.Request.Item;

import netUtils.Request.BaseRequest;

public class DeleteItemRequest extends BaseRequest
{
	public DeleteItemRequest(int itemID)
	{
		super();
		
		String requestUrl = getUrl();
		requestUrl += "/item/" + itemID;
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

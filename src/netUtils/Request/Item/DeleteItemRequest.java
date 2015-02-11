package netUtils.Request.Item;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

public class DeleteItemRequest extends BaseRequest
{
	public DeleteItemRequest(int itemID)
	{
		super();

		appendUrl(URLDef.URL_ITEM + "/" + itemID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

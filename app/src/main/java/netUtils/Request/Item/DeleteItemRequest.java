package netUtils.request.item;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class DeleteItemRequest extends BaseRequest
{
	public DeleteItemRequest(int itemID)
	{
		super();

        appendUrl(URLDef.URL_ITEM);
        appendUrl(itemID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

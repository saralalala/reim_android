package netUtils.Request.Item;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

public class GetItemsRequest extends BaseRequest
{
	public GetItemsRequest(int pageIndex, int pageSize)
	{
		appendUrl(URLDef.URL_ITEM + "/" + pageIndex + "/" + pageSize);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

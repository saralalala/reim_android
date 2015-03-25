package netUtils.request.item;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

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

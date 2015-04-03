package netUtils.request.item;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class GetItemsRequest extends BaseRequest
{
	public GetItemsRequest(int pageIndex, int pageSize)
	{
        super();

        appendUrl(URLDef.URL_ITEM);
        appendUrl(pageIndex);
        appendUrl(pageSize);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

package netUtils.Request.Item;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class GetItemsRequest extends BaseRequest
{
	public GetItemsRequest(int pageIndex, int pageSize)
	{
		String urlSuffix = "/item/" + pageIndex + "/" + pageSize;
		appendUrl(urlSuffix);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

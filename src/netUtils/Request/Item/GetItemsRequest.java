package netUtils.Request.Item;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class GetItemsRequest extends BaseRequest
{
	public GetItemsRequest(int pageIndex, int pageSize)
	{
		String requestUrl = getUrl();
		requestUrl += "/item/" + pageIndex + "/" + pageSize;
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

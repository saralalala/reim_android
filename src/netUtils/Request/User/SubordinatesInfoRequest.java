package netUtils.Request.User;

import netUtils.Request.BaseRequest;

public class SubordinatesInfoRequest extends BaseRequest
{
	public SubordinatesInfoRequest(int pageIndex, int pageSize)
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/subordinate/" + pageIndex + "/" + pageSize;
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

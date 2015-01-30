package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class SubordinatesInfoRequest extends BaseRequest
{
	public SubordinatesInfoRequest(int pageIndex, int pageSize)
	{
		super();

		appendUrl("/subordinate/" + pageIndex + "/" + pageSize);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

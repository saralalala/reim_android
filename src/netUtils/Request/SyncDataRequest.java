package netUtils.Request;

import java.sql.Date;

import netUtils.HttpConnectionCallback;

public class SyncDataRequest extends BaseRequest
{
	public SyncDataRequest(int timeStamp)
	{
		super();
		
		String requestUrl = getUrl();
		requestUrl += "/sync/" + timeStamp;
		setUrl(requestUrl);
	}
	
	public SyncDataRequest(int pageIndex, int pageSize, Date startDate, Date endDate)
	{
		super();
		
		String requestUrl = getUrl();
		requestUrl += "/item/" + startDate.getTime() + "/" + endDate.getTime() + "/" + pageIndex + "/" + pageSize;
		setUrl(requestUrl);		
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

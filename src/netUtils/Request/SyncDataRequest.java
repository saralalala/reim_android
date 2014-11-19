package netUtils.Request;

import java.sql.Date;

import netUtils.HttpConnectionCallback;

public class SyncDataRequest extends BaseRequest
{
	public SyncDataRequest(int timeStamp)
	{
		super();
		
		String urlSuffix = "/sync/" + timeStamp;
		appendUrl(urlSuffix);
	}
	
	public SyncDataRequest(int pageIndex, int pageSize, Date startDate, Date endDate)
	{
		super();
		
		String urlSuffix = "/item/" + startDate.getTime() + "/" + endDate.getTime() + "/" + pageIndex + "/" + pageSize;
		appendUrl(urlSuffix);	
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

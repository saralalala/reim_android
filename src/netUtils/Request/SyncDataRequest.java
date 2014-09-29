package netUtils.Request;

import java.sql.Date;


public class SyncDataRequest extends BaseRequest
{
	public SyncDataRequest()
	{
		super();
		
		String requestUrl = getUrl();
		requestUrl += "/sync/0";
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

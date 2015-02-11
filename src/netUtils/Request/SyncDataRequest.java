package netUtils.Request;

import java.sql.Date;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;

public class SyncDataRequest extends BaseRequest
{
	public SyncDataRequest(int timeStamp)
	{
		super();
		
		appendUrl(URLDef.URL_SYNC + "/" + timeStamp);
	}
	
	public SyncDataRequest(int pageIndex, int pageSize, Date startDate, Date endDate)
	{
		super();
		
		appendUrl(URLDef.URL_ITEM + "/" + startDate.getTime() + "/" + endDate.getTime() + "/" + pageIndex + "/" + pageSize);	
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

package netUtils.request;

import java.sql.Date;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;

public class SyncDataRequest extends BaseRequest
{
	public SyncDataRequest(int timeStamp)
	{
		super();

        appendUrl(URLDef.URL_SYNC);
        appendUrl(timeStamp);
	}
	
	public SyncDataRequest(int pageIndex, int pageSize, Date startDate, Date endDate)
	{
		super();

        appendUrl(URLDef.URL_ITEM);
        appendUrl(startDate.getTime());
        appendUrl(endDate.getTime());
        appendUrl(pageIndex);
        appendUrl(pageSize);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

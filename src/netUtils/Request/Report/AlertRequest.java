package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

public class AlertRequest extends BaseRequest
{
	public AlertRequest(int userID, int reportID)
	{
		super();

		appendUrl(URLDef.URL_ALERT + "/" + userID + "/" + reportID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
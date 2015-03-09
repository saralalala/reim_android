package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import netUtils.URLDef;

public class GetReportRequest extends BaseRequest
{
	public GetReportRequest(int reportID)
	{
		super();

		appendUrl(URLDef.URL_REPORT + "/" + reportID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
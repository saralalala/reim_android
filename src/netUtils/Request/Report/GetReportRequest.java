package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

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
package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

public class DeleteReportRequest extends BaseRequest
{
	public DeleteReportRequest(int reportID)
	{
		super();

		appendUrl(URLDef.URL_REPORT + "/" + reportID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}
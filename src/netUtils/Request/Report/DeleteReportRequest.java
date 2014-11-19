package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class DeleteReportRequest extends BaseRequest
{
	public DeleteReportRequest(int reportID)
	{
		super();

		String urlSuffix = "/report/" + reportID;
		appendUrl(urlSuffix);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}
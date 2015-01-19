package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class DeleteReportRequest extends BaseRequest
{
	public DeleteReportRequest(int reportID)
	{
		super();

		appendUrl("/report/" + reportID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}
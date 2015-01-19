package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class GetReportRequest extends BaseRequest
{
	public GetReportRequest(int reportID)
	{
		super();

		appendUrl("/report/" + reportID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class ApproveInfoRequest extends BaseRequest
{
	public ApproveInfoRequest(int reportID)
	{
		super();

		appendUrl("/report_flow/" + reportID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
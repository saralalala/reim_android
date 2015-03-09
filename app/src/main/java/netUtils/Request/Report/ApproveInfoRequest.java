package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import netUtils.URLDef;

public class ApproveInfoRequest extends BaseRequest
{
	public ApproveInfoRequest(int reportID)
	{
		super();

		appendUrl(URLDef.URL_APPROVE_INFO + "/" + reportID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
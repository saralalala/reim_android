package netUtils.request.report;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ApproveInfoRequest extends BaseRequest
{
	public ApproveInfoRequest(int reportID)
	{
		super();

        appendUrl(URLDef.URL_APPROVE_INFO);
        appendUrl(reportID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
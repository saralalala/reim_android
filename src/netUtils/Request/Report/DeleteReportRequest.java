package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class DeleteReportRequest extends BaseRequest
{
	public DeleteReportRequest(int reportID)
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/report/" + reportID;
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}
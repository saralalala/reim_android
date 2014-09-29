package netUtils.Request.Report;

import classes.Report;
import netUtils.Request.BaseRequest;

public class DeleteReportRequest extends BaseRequest
{
	public DeleteReportRequest(Report report)
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/report/" + report.getId();
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}

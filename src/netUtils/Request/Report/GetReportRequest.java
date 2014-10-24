package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class GetReportRequest extends BaseRequest
{
	public GetReportRequest(int reportID)
	{
		super();

		String requestUrl = getUrl();
		requestUrl += "/report/" + reportID;
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
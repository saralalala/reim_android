package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class SubordinatesReportRequest extends BaseRequest
{
	public SubordinatesReportRequest(int pageIndex, int pageSize, int status)
	{
		super();
		
		String requestUrl = getUrl();
		requestUrl += "/subordinate_reports/1/" + pageIndex + "/" + pageSize + "/" + status;
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

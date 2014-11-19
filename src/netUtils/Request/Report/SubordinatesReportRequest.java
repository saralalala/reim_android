package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class SubordinatesReportRequest extends BaseRequest
{
	public SubordinatesReportRequest(int pageIndex, int pageSize, int status)
	{
		super();
		
		String urlSuffix = "/subordinate_reports/1/" + pageIndex + "/" + pageSize + "/" + status;
		appendUrl(urlSuffix);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

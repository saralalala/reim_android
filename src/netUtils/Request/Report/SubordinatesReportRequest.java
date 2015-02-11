package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

public class SubordinatesReportRequest extends BaseRequest
{
	public SubordinatesReportRequest(int pageIndex, int pageSize, int status)
	{
		super();
		
		appendUrl(URLDef.URL_SUBORDINATE_REPORT + "/1/" + pageIndex + "/" + pageSize + "/" + status);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

package netUtils.request.report;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class SubordinatesReportRequest extends BaseRequest
{
	public SubordinatesReportRequest(int pageIndex, int pageSize, int status)
	{
		super();

        appendUrl(URLDef.URL_SUBORDINATE_REPORT);
        appendUrl(1);
        appendUrl(pageIndex);
        appendUrl(pageSize);
        appendUrl(status);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

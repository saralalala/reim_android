package netUtils.request.report;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

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

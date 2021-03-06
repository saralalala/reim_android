package netUtils.request.report;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class GetReportRequest extends BaseRequest
{
    public GetReportRequest(int reportID)
    {
        super();

        appendUrl(URLDef.URL_REPORT);
        appendUrl(reportID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
package netUtils.request.report;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class DeleteReportRequest extends BaseRequest
{
    public DeleteReportRequest(int reportID)
    {
        super();

        appendUrl(URLDef.URL_REPORT);
        appendUrl(reportID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doDelete(callback);
    }
}
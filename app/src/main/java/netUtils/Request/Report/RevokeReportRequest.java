package netUtils.request.report;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class RevokeReportRequest extends BaseRequest
{
    public RevokeReportRequest(int reportID)
    {
        super();

        appendUrl(URLDef.URL_REVOKE);
        appendUrl(reportID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
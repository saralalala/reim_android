package netUtils.request.report;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ConfirmReportRequest extends BaseRequest
{
    public ConfirmReportRequest(int reportID)
    {
        super();

        addParams("act", "confirm");
        addParams("rids", reportID);
        addParams("status", 2);

        appendUrl(URLDef.URL_CONFIRM_REPORT);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}
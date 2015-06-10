package netUtils.request.report;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ApproveInfoRequest extends BaseRequest
{
    public ApproveInfoRequest(int reportID)
    {
        super();

        appendUrl(URLDef.URL_APPROVE_INFO);
        appendUrl(reportID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
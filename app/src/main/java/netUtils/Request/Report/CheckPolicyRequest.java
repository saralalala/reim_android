package netUtils.request.report;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class CheckPolicyRequest extends BaseRequest
{
    public CheckPolicyRequest(int reportID)
    {
        super();

        appendUrl(URLDef.URL_CHECK_POLICY);
        appendUrl(reportID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
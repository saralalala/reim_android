package netUtils.request.report;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class SingleSubReportRequest extends BaseRequest
{
    public SingleSubReportRequest(int pageIndex, int pageSize, int userID, int status)
    {
        super();

        addParams("uid", Integer.toString(userID));
        addParams("status", Integer.toString(status));

        appendUrl(URLDef.URL_SUBORDINATE_REPORT);
        appendUrl(pageIndex);
        appendUrl(pageSize);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}

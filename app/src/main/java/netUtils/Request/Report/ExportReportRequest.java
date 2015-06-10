package netUtils.request.report;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ExportReportRequest extends BaseRequest
{
    public ExportReportRequest(int reportID, String email)
    {
        super();

        addParams("rid", Integer.toString(reportID));
        addParams("email", email);

        appendUrl(URLDef.URL_EXPORT);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}

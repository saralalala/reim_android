package netUtils.request.report;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

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

package netUtils.request.report;

import classes.model.Report;
import classes.model.User;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ApproveReportRequest extends BaseRequest
{
    public ApproveReportRequest(Report report, boolean isFinished)
    {
        super();

        addParams("status", Integer.toString(report.getMyDecision()));
        if (!isFinished)
        {
            addParams("manager_id", User.getUsersIDString(report.getManagerList()));
            addParams("cc", User.getUsersIDString(report.getCCList()));
        }
        appendUrl(URLDef.URL_REPORT);
        appendUrl(report.getServerID());
    }

    public ApproveReportRequest(Report report, String commentContent)
    {
        super();

        addParams("status", Integer.toString(report.getMyDecision()));
        addParams("manager_id", "");
        addParams("cc", "");
        addParams("comment", commentContent);

        appendUrl(URLDef.URL_REPORT);
        appendUrl(report.getServerID());
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}
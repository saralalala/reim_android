package netUtils.request.report;

import classes.model.Report;
import classes.model.User;
import classes.utils.DBManager;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ModifyReportRequest extends BaseRequest
{
    public ModifyReportRequest(Report report)
    {
        super();

        DBManager dbManager = DBManager.getDBManager();
        String iids = dbManager.getReportItemIDs(report.getLocalID());

        addParams("title", report.getTitle());
        addParams("iids", iids);
        addParams("status", Integer.toString(report.getStatus()));
        addParams("manager_id", User.getUsersIDString(report.getManagerList()));
        addParams("cc", User.getUsersIDString(report.getCCList()));

        appendUrl(URLDef.URL_REPORT);
        appendUrl(report.getServerID());
    }

    public ModifyReportRequest(Report report, String commentContent)
    {
        super();

        DBManager dbManager = DBManager.getDBManager();
        String iids = dbManager.getReportItemIDs(report.getLocalID());

        addParams("title", report.getTitle());
        addParams("iids", iids);
        addParams("status", Integer.toString(report.getStatus()));
        addParams("manager_id", User.getUsersIDString(report.getManagerList()));
        addParams("cc", User.getUsersIDString(report.getCCList()));
        addParams("comment", commentContent);

        appendUrl(URLDef.URL_REPORT);
        appendUrl(report.getServerID());
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}
package netUtils.request.report;

import classes.model.Report;
import classes.model.User;
import classes.utils.DBManager;
import classes.utils.Utils;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ModifyReportRequest extends BaseRequest
{
    public ModifyReportRequest(Report report, boolean forceSubmit)
    {
        super();

        DBManager dbManager = DBManager.getDBManager();
        String iids = dbManager.getReportItemIDs(report.getLocalID());

        addParams("title", report.getTitle());
        addParams("iids", iids);
        addParams("status", Integer.toString(report.getStatus()));
        addParams("manager_id", User.getUsersIDString(report.getManagerList()));
        addParams("cc", User.getUsersIDString(report.getCCList()));
        addParams("force_submit", Utils.booleanToInt(forceSubmit));

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
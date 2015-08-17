package netUtils.request.report;

import classes.model.Report;
import classes.model.User;
import classes.utils.DBManager;
import classes.utils.Utils;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class CreateReportRequest extends BaseRequest
{
    public CreateReportRequest(Report report, boolean forceSubmit)
    {
        super();

        DBManager dbManager = DBManager.getDBManager();
        String iids = dbManager.getReportItemIDs(report.getLocalID());

        addParams("title", report.getTitle());
        addParams("iids", iids);
        addParams("status", Integer.toString(report.getStatus()));
        addParams("manager_id", User.getUsersIDString(report.getManagerList()));
        addParams("cc", User.getUsersIDString(report.getCCList()));
        addParams("prove_ahead", Integer.toString(report.getType()));
        addParams("createdt", Integer.toString(report.getCreatedDate()));
        addParams("force_submit", Utils.booleanToInt(forceSubmit));

        appendUrl(URLDef.URL_REPORT);
    }

    public CreateReportRequest(Report report, String commentContent)
    {
        super();

        DBManager dbManager = DBManager.getDBManager();
        String iids = dbManager.getReportItemIDs(report.getLocalID());

        addParams("title", report.getTitle());
        addParams("iids", iids);
        addParams("status", Integer.toString(report.getStatus()));
        addParams("manager_id", User.getUsersIDString(report.getManagerList()));
        addParams("cc", User.getUsersIDString(report.getCCList()));
        addParams("prove_ahead", Integer.toString(report.getType()));
        addParams("createdt", Integer.toString(report.getCreatedDate()));
        addParams("comment", commentContent);

        appendUrl(URLDef.URL_REPORT);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
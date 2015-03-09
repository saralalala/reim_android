package netUtils.Request.Report;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import classes.Report;
import classes.User;
import classes.utils.DBManager;
import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import netUtils.URLDef;

public class ModifyReportRequest extends BaseRequest
{
	public ModifyReportRequest(Report report)
	{
		super();
		
		DBManager dbManager = DBManager.getDBManager();
		String iids = dbManager.getReportItemIDs(report.getLocalID());
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("title", report.getTitle()));
		params.add(new BasicNameValuePair("iids", iids));
		params.add(new BasicNameValuePair("status", Integer.toString(report.getStatus())));
		params.add(new BasicNameValuePair("manager_id", User.getUsersIDString(report.getManagerList())));
		params.add(new BasicNameValuePair("cc", User.getUsersIDString(report.getCCList())));
		setParams(params);

		appendUrl(URLDef.URL_REPORT + "/" + report.getServerID());
	}
	
	public ModifyReportRequest(Report report, String commentContent)
	{
		super();
		
		DBManager dbManager = DBManager.getDBManager();
		String iids = dbManager.getReportItemIDs(report.getLocalID());
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("title", report.getTitle()));
		params.add(new BasicNameValuePair("iids", iids));
		params.add(new BasicNameValuePair("status", Integer.toString(report.getStatus())));
		params.add(new BasicNameValuePair("manager_id", User.getUsersIDString(report.getManagerList())));
		params.add(new BasicNameValuePair("cc", User.getUsersIDString(report.getCCList())));
		params.add(new BasicNameValuePair("comment", commentContent));
		setParams(params);

		appendUrl(URLDef.URL_REPORT + "/" + report.getServerID());
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}
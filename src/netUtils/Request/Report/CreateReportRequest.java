package netUtils.Request.Report;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import classes.Report;
import classes.User;
import classes.Utils.Utils;
import database.DBManager;
import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class CreateReportRequest extends BaseRequest
{
	public CreateReportRequest(Report report)
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
		params.add(new BasicNameValuePair("prove_ahead", Utils.booleanToString(report.isProveAhead())));
		setParams(params);

		appendUrl("/report");
	}
	
	public CreateReportRequest(Report report, String commentContent)
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
		params.add(new BasicNameValuePair("prove_ahead", Utils.booleanToString(report.isProveAhead())));
		params.add(new BasicNameValuePair("comment", commentContent));
		setParams(params);

		String requestUrl = getUrl();
		requestUrl += "/report";
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}
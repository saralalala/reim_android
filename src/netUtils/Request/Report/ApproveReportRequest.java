package netUtils.Request.Report;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import classes.Report;
import classes.User;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

public class ApproveReportRequest extends BaseRequest
{	
	public ApproveReportRequest(Report report, String commentContent)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("status", Integer.toString(report.getMyDecision())));
		params.add(new BasicNameValuePair("manager_id", ""));
		params.add(new BasicNameValuePair("cc", ""));
		params.add(new BasicNameValuePair("comment", commentContent));
		setParams(params);

		appendUrl(URLDef.URL_REPORT + "/" + report.getServerID());
	}
	
	public ApproveReportRequest(Report report)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("manager_id", User.getUsersIDString(report.getManagerList())));
		params.add(new BasicNameValuePair("cc", User.getUsersIDString(report.getCCList())));
		setParams(params);

		appendUrl(URLDef.URL_REPORT + "/" + report.getServerID());
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}
package netUtils.Request.Report;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import classes.Report;
import netUtils.Request.BaseRequest;

public class ModifyReportRequest extends BaseRequest
{
	public ModifyReportRequest(Report report)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("title", report.getTitle()));
		params.add(new BasicNameValuePair("iids", report.getItemIDs()));
		params.add(new BasicNameValuePair("status", Integer.toString(report.getStatus())));
		setParams(params);

		String requestUrl = getUrl();
		requestUrl += "/report/" + report.getId();
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

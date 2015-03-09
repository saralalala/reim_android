package netUtils.Request.Report;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import netUtils.URLDef;

public class ExportReportRequest extends BaseRequest
{
	public ExportReportRequest(int reportID, String email)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("rid", Integer.toString(reportID)));
		params.add(new BasicNameValuePair("email", email));
		setParams(params);

		appendUrl(URLDef.URL_EXPORT);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}

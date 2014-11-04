package netUtils.Request.Report;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class ExportReportRequest extends BaseRequest
{
	public ExportReportRequest(int reportID, String email)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("rid", Integer.toString(reportID)));
		params.add(new BasicNameValuePair("email", email));
		setParams(params);

		String requestUrl = getUrl();
		requestUrl += "/exports";
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}

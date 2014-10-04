package netUtils.Request.Report;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.Request.BaseRequest;

public class SubordinatesReportRequest extends BaseRequest
{
	public SubordinatesReportRequest(int pageIndex, int pageSize, int status)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("status", Integer.toString(status)));
		setParams(params);
		String requestUrl = getUrl();
		requestUrl += "/subordinate_reports/" + pageIndex + "/" + pageSize;
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

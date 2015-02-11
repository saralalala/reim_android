package netUtils.Request.Report;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

public class SingleSubReportRequest extends BaseRequest
{
	public SingleSubReportRequest(int pageIndex, int pageSize, int userID, int status)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("uid", Integer.toString(userID)));
		params.add(new BasicNameValuePair("status", Integer.toString(status)));
		setParams(params);
		
		appendUrl(URLDef.URL_SUBORDINATE_REPORT + "/" + pageIndex + "/" + pageSize);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}

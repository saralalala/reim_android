package netUtils.Request;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class FeedbackRequest extends BaseRequest
{
	public FeedbackRequest(String content, String contactInfo, String appVersion)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("content", content));
		params.add(new BasicNameValuePair("contact", contactInfo));
		params.add(new BasicNameValuePair("version", appVersion));
		params.add(new BasicNameValuePair("platform", Integer.toString(2)));
		setParams(params);

		appendUrl(URLDef.URL_FEEDBACK);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}

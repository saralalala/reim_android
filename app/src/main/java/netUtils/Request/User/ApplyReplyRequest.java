package netUtils.request.user;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ApplyReplyRequest extends BaseRequest
{
	public ApplyReplyRequest(int applyID, int agree)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("permit", Integer.toString(agree)));
		setParams(params);

		appendUrl(URLDef.URL_APPLY);
        appendUrl(applyID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

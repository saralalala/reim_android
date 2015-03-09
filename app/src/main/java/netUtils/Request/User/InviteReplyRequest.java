package netUtils.Request.User;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import netUtils.URLDef;

public class InviteReplyRequest extends BaseRequest
{
	public InviteReplyRequest(int agree, String inviteCode)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("status", Integer.toString(agree)));
		params.add(new BasicNameValuePair("code", inviteCode));
		setParams(params);

		appendUrl(URLDef.URL_INVITE);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

package netUtils.Request.User;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class InviteReplyRequest extends BaseRequest
{
	public InviteReplyRequest(int agree, int inviteCode)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("status", Integer.toString(agree)));
		params.add(new BasicNameValuePair("code", Integer.toString(inviteCode)));
		setParams(params);

		String requestUrl = getUrl();
		requestUrl += "/invite";
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

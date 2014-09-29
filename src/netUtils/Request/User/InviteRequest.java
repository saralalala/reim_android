package netUtils.Request.User;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.Request.BaseRequest;

public class InviteRequest extends BaseRequest
{
	public InviteRequest(int type, String arg)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("type", Integer.toString(type)));
		params.add(new BasicNameValuePair("name", arg));
		setParams(params);

		String requestUrl = getUrl();
		requestUrl += "/invite";
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}
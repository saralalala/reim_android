package netUtils.Request.User;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

public class ForgotPasswordRequest extends BaseRequest
{
	public ForgotPasswordRequest(int type, String arg)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("type", Integer.toString(type)));
		params.add(new BasicNameValuePair("name", arg));
		setParams(params);
		
		appendUrl(URLDef.URL_PASSWORD);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}
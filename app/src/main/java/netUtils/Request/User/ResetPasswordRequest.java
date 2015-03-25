package netUtils.request.user;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ResetPasswordRequest extends BaseRequest
{
	public ResetPasswordRequest(String password, int codeID, String code)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("cid", Integer.toString(codeID)));
		params.add(new BasicNameValuePair("code", code));
		setParams(params);

		appendUrl(URLDef.URL_PASSWORD);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

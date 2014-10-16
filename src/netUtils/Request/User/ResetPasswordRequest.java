package netUtils.Request.User;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

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
		
		String requestUrl = getUrl();
		setUrl(requestUrl += "/password");
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

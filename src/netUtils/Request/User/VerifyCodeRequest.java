package netUtils.Request.User;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class VerifyCodeRequest extends BaseRequest
{
	public VerifyCodeRequest(String phone)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("phone", phone));
		setParams(params);
		
		appendUrl("/vcode");
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}
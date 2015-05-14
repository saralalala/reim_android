package netUtils.request.user;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import classes.model.User;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class RegisterRequest extends BaseRequest
{
	public RegisterRequest(User user, String verifyCode)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("email", user.getEmail()));
		params.add(new BasicNameValuePair("phone", user.getPhone()));
		params.add(new BasicNameValuePair("password", user.getPassword()));	
		params.add(new BasicNameValuePair("code", verifyCode));	
		setParams(params);

		appendUrl(URLDef.URL_USER);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}

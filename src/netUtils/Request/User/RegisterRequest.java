package netUtils.Request.User;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import classes.User;

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

		appendUrl("/users");
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}

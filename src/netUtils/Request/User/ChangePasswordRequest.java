package netUtils.Request.User;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class ChangePasswordRequest extends BaseRequest
{
	public ChangePasswordRequest(String oldPassword, String newPassword)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("old_password", oldPassword));
		params.add(new BasicNameValuePair("new_password", newPassword));
		setParams(params);

		appendUrl("/users");
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}
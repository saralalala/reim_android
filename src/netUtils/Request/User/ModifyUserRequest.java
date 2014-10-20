package netUtils.Request.User;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import classes.User;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class ModifyUserRequest extends BaseRequest
{
	public ModifyUserRequest(User user)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("email", user.getEmail()));
		params.add(new BasicNameValuePair("phone", user.getPhone()));
		params.add(new BasicNameValuePair("nickname", user.getNickname()));
		setParams(params);

		String requestUrl = getUrl();
		requestUrl += "/users";
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

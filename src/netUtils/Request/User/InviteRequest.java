package netUtils.Request.User;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class InviteRequest extends BaseRequest
{
	public InviteRequest(String username)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", username));
		setParams(params);

		appendUrl("/invite");
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}
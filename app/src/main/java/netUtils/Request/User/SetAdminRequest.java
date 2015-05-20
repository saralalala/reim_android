package netUtils.request.user;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import classes.model.User;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class SetAdminRequest extends BaseRequest
{
	public SetAdminRequest(List<User> userList)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("admin", Integer.toString(2)));
		params.add(new BasicNameValuePair("uid", User.getUsersIDString(userList)));
		setParams(params);

		appendUrl(URLDef.URL_SET_ADMIN);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}

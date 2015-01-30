package netUtils.Request.User;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import classes.utils.AppPreference;

public class DefaultManagerRequest extends BaseRequest
{
	public DefaultManagerRequest(int defaultManagerID)
	{
		super();
		
		String phone = AppPreference.getAppPreference().getCurrentUser().getPhone();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("manager_id", Integer.toString(defaultManagerID)));
		params.add(new BasicNameValuePair("phone", phone));
		setParams(params);

		appendUrl("/users");
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

package netUtils.Request.User;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class DefaultManagerRequest extends BaseRequest
{
	public DefaultManagerRequest(int defaultManagerID)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("manager_id", Integer.toString(defaultManagerID)));
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

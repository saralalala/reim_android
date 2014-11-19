package netUtils.Request.Group;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class CreateGroupRequest extends BaseRequest
{
	public CreateGroupRequest(String groupName)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", groupName));
		setParams(params);

		appendUrl("/groups");
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}

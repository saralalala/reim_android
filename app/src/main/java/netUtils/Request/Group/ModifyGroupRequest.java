package netUtils.request.group;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ModifyGroupRequest extends BaseRequest
{
	public ModifyGroupRequest(String groupName)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", groupName));
		setParams(params);

		appendUrl(URLDef.URL_GROUP);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

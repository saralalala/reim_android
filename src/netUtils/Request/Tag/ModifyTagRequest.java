package netUtils.Request.Tag;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import classes.Tag;

import netUtils.Request.BaseRequest;

public class ModifyTagRequest extends BaseRequest
{
	public ModifyTagRequest(Tag tag)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", tag.getName()));
		setParams(params);

		String requestUrl = getUrl();
		requestUrl += "/tags/" + tag.getServerID();
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

package netUtils.Request.Tag;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import classes.Tag;

import netUtils.Request.BaseRequest;

public class CreateTagRequest extends BaseRequest
{
	public CreateTagRequest(Tag tag)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", tag.getName()));
		setParams(params);

		String requestUrl = getUrl();
		requestUrl += "/tags";
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}

}

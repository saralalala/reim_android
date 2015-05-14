package netUtils.request.tag;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import classes.model.Tag;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ModifyTagRequest extends BaseRequest
{
	public ModifyTagRequest(Tag tag)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", tag.getName()));
		setParams(params);

        appendUrl(URLDef.URL_TAG);
        appendUrl(tag.getServerID());
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

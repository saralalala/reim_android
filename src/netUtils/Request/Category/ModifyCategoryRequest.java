package netUtils.Request.Category;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import classes.Category;

import netUtils.Request.BaseRequest;

public class ModifyCategoryRequest extends BaseRequest
{
	public ModifyCategoryRequest(Category category)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", category.getName()));
		params.add(new BasicNameValuePair("limit", Double.toString(category.getLimit())));
		params.add(new BasicNameValuePair("pid", Integer.toString(category.getParentID())));
		params.add(new BasicNameValuePair("gid", Integer.toString(category.getGroupID())));
		params.add(new BasicNameValuePair("pb", category.isProveAhead().toString()));
		setParams(params);

		String requestUrl = getUrl();
		requestUrl += "/category/" + category.getId();
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

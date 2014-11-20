package netUtils.Request.Category;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import classes.Category;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class ModifyCategoryRequest extends BaseRequest
{
	public ModifyCategoryRequest(Category category)
	{
		super();

		String pbFlag = category.isProveAhead() ? "1" : "0";
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", category.getName()));
		params.add(new BasicNameValuePair("limit", Double.toString(category.getLimit())));
		params.add(new BasicNameValuePair("pid", Integer.toString(category.getParentID())));
		params.add(new BasicNameValuePair("gid", Integer.toString(category.getGroupID())));
		params.add(new BasicNameValuePair("pb", pbFlag));
		setParams(params);

		String urlSuffix = "/category/" + category.getServerID();
		appendUrl(urlSuffix);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

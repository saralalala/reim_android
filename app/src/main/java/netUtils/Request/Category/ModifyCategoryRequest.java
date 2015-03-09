package netUtils.Request.Category;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import classes.Category;
import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;
import netUtils.URLDef;

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
		params.add(new BasicNameValuePair("avatar", Integer.toString(category.getIconID())));
		setParams(params);

		appendUrl(URLDef.URL_CATEGORY + "/" + category.getServerID());
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}

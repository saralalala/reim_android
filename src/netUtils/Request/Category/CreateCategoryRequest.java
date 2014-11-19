package netUtils.Request.Category;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import classes.Category;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class CreateCategoryRequest extends BaseRequest
{
	public CreateCategoryRequest(Category category)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", category.getName()));
		params.add(new BasicNameValuePair("limit", Double.toString(category.getLimit())));
		params.add(new BasicNameValuePair("pid", Integer.toString(category.getParentID())));
		params.add(new BasicNameValuePair("gid", Integer.toString(category.getGroupID())));
		params.add(new BasicNameValuePair("pb", category.isProveAhead().toString()));
		setParams(params);

		appendUrl("/category");
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}

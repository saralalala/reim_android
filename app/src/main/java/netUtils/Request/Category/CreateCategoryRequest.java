package netUtils.request.category;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import classes.model.Category;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

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
        params.add(new BasicNameValuePair("pb", Integer.toString(category.getType())));
        params.add(new BasicNameValuePair("avatar", Integer.toString(category.getIconID())));
        setParams(params);

        appendUrl(URLDef.URL_CATEGORY);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}

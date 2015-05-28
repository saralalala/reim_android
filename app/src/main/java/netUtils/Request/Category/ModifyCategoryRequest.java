package netUtils.request.category;

import classes.model.Category;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ModifyCategoryRequest extends BaseRequest
{
    public ModifyCategoryRequest(Category category)
    {
        super();

        addParams("name", category.getName());
        addParams("limit", Double.toString(category.getLimit()));
        addParams("pid", Integer.toString(category.getParentID()));
        addParams("gid", Integer.toString(category.getGroupID()));
        addParams("pb", Integer.toString(category.getType()));
        addParams("avatar", Integer.toString(category.getIconID()));

        appendUrl(URLDef.URL_CATEGORY);
        appendUrl(category.getServerID());
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}

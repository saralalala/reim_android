package netUtils.request.category;

import classes.model.Category;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class CreateCategoryRequest extends BaseRequest
{
    public CreateCategoryRequest(Category category)
    {
        super();

        addParams("name", category.getName());
        addParams("limit", Double.toString(category.getLimit()));
        addParams("pid", Integer.toString(category.getParentID()));
        addParams("gid", Integer.toString(category.getGroupID()));
        addParams("pb", Integer.toString(category.getType()));
        addParams("avatar", Integer.toString(category.getIconID()));
        
        appendUrl(URLDef.URL_CATEGORY);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}

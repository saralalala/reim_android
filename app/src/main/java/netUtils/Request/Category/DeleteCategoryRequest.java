package netUtils.request.category;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class DeleteCategoryRequest extends BaseRequest
{
    public DeleteCategoryRequest(int categoryID)
    {
        super();

        appendUrl(URLDef.URL_CATEGORY);
        appendUrl(categoryID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doDelete(callback);
    }
}

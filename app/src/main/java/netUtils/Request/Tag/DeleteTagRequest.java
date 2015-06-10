package netUtils.request.tag;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class DeleteTagRequest extends BaseRequest
{
    public DeleteTagRequest(int tagID)
    {
        super();

        appendUrl(URLDef.URL_TAG);
        appendUrl(tagID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doDelete(callback);
    }
}

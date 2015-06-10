package netUtils.request.tag;

import classes.model.Tag;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ModifyTagRequest extends BaseRequest
{
    public ModifyTagRequest(Tag tag)
    {
        super();

        addParams("name", tag.getName());

        appendUrl(URLDef.URL_TAG);
        appendUrl(tag.getServerID());
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}

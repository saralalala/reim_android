package netUtils.request.tag;

import classes.model.Tag;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class CreateTagRequest extends BaseRequest
{
    public CreateTagRequest(Tag tag)
    {
        super();

        addParams("name", tag.getName());

        appendUrl(URLDef.URL_TAG);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}

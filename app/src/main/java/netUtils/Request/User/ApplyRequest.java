package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ApplyRequest extends BaseRequest
{
    public ApplyRequest(int groupID, int guideVersion)
    {
        super();

        addParams("gid", Integer.toString(groupID));
        addParams("version", Integer.toString(guideVersion));

        appendUrl(URLDef.URL_APPLY);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
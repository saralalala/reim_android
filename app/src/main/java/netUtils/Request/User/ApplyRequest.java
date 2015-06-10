package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

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
package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ApplyReplyRequest extends BaseRequest
{
    public ApplyReplyRequest(int applyID, int agree)
    {
        super();

        addParams("permit", Integer.toString(agree));

        appendUrl(URLDef.URL_APPLY);
        appendUrl(applyID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}

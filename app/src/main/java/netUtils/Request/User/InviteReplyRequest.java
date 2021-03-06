package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class InviteReplyRequest extends BaseRequest
{
    public InviteReplyRequest(int agree, String inviteCode)
    {
        super();

        addParams("status", Integer.toString(agree));
        addParams("code", inviteCode);

        appendUrl(URLDef.URL_INVITE);
    }

    public InviteReplyRequest(int agree, String inviteCode, int guideVersion)
    {
        super();

        addParams("status", Integer.toString(agree));
        addParams("code", inviteCode);
        addParams("version", Integer.toString(guideVersion));

        appendUrl(URLDef.URL_INVITE);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}

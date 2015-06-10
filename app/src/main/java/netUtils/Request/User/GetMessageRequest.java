package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class GetMessageRequest extends BaseRequest
{
    public GetMessageRequest(int type, int messageID)
    {
        super();

        appendUrl(URLDef.URL_MESSAGE);
        appendUrl(type);
        appendUrl(messageID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
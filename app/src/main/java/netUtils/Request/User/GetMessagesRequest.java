package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class GetMessagesRequest extends BaseRequest
{
    public GetMessagesRequest()
    {
        super();

        appendUrl(URLDef.URL_MESSAGE_LIST);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
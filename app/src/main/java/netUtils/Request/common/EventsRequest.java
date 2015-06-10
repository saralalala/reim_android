package netUtils.request.common;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;

public class EventsRequest extends BaseRequest
{
    public EventsRequest()
    {
        super();

        appendUrl(URLDef.URL_EVENT);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
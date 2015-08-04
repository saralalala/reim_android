package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class UberHistoryRequest extends BaseRequest
{
    public UberHistoryRequest()
    {
        super();

        setUrl(URLDef.URL_UBER_HISTORY);
    }

    public UberHistoryRequest(int offset)
    {
        super();

        addParams("offset", offset);

        setUrl(URLDef.URL_UBER_HISTORY);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}

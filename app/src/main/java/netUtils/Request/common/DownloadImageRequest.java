package netUtils.request.common;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;

public class DownloadImageRequest extends BaseRequest
{
    public DownloadImageRequest(String url)
    {
        super();

        setUrl(url);
    }

    public DownloadImageRequest(int iconID)
    {
        super();

        appendUrl(URLDef.URL_STATIC);
        appendUrl(iconID + ".png");
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doDownloadBinary(callback);
    }
}
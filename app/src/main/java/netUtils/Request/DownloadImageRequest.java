package netUtils.request;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;

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
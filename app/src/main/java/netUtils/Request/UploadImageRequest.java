package netUtils.request;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;

public class UploadImageRequest extends BaseRequest
{
    public UploadImageRequest(String path, int type)
    {
        super();

        addParams("name", "filePath");
        addParams("filePath", path);
        addParams("type", Integer.toString(type));

        appendUrl(URLDef.URL_IMAGE);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}

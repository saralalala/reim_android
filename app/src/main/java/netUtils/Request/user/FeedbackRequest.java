package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class FeedbackRequest extends BaseRequest
{
    public FeedbackRequest(String content, String contactInfo, String appVersion)
    {
        super();

        addParams("content", content);
        addParams("contact", contactInfo);
        addParams("version", appVersion);
        addParams("platform", Integer.toString(2));

        appendUrl(URLDef.URL_FEEDBACK);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}

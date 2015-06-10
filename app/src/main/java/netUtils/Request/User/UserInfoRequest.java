package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class UserInfoRequest extends BaseRequest
{
    public UserInfoRequest(int userID)
    {
        super();

        appendUrl(URLDef.URL_USER);
        appendUrl(userID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}

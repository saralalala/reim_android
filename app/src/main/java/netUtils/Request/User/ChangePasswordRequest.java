package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ChangePasswordRequest extends BaseRequest
{
    public ChangePasswordRequest(String oldPassword, String newPassword)
    {
        super();

        addParams("old_password", oldPassword);
        addParams("new_password", newPassword);

        appendUrl(URLDef.URL_USER);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}
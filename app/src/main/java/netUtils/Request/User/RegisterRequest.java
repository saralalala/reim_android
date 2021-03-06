package netUtils.request.user;

import classes.model.User;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class RegisterRequest extends BaseRequest
{
    public RegisterRequest(User user)
    {
        super();

        addParams("email", user.getEmail());
        addParams("password", user.getPassword());

        appendUrl(URLDef.URL_USER);
    }

    public RegisterRequest(User user, String verifyCode)
    {
        super();

        addParams("phone", user.getPhone());
        addParams("password", user.getPassword());
        addParams("vcode", verifyCode);

        appendUrl(URLDef.URL_USER);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
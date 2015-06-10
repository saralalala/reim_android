package netUtils.request.user;

import classes.model.User;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ModifyUserRequest extends BaseRequest
{
    public ModifyUserRequest(User user)
    {
        super();

        addParams("email", user.getEmail());
        addParams("phone", user.getPhone());
        addParams("nickname", user.getNickname());

        appendUrl(URLDef.URL_USER);
    }

    public ModifyUserRequest(User user, String verifyCode)
    {
        super();

        addParams("phone", user.getPhone());
        addParams("vcode", verifyCode);

        appendUrl(URLDef.URL_USER);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}

package netUtils.request.user;

import java.util.List;

import classes.model.User;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class SetAdminRequest extends BaseRequest
{
    public SetAdminRequest(List<User> userList)
    {
        super();

        addParams("admin", Integer.toString(2));
        addParams("uid", User.getUsersIDString(userList));

        appendUrl(URLDef.URL_SET_ADMIN);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}

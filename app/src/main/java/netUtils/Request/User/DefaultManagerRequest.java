package netUtils.request.user;

import classes.utils.AppPreference;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class DefaultManagerRequest extends BaseRequest
{
    public DefaultManagerRequest(int defaultManagerID)
    {
        super();

        String phone = AppPreference.getAppPreference().getCurrentUser().getPhone();
        addParams("manager_id", Integer.toString(defaultManagerID));
        addParams("phone", phone);

        appendUrl(URLDef.URL_USER);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}

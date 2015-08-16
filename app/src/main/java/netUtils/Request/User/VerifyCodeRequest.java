package netUtils.request.user;

import classes.utils.Utils;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class VerifyCodeRequest extends BaseRequest
{
    public VerifyCodeRequest(String phone, boolean isBind)
    {
        super();

        addParams("phone", phone);
        addParams("bind", Utils.booleanToInt(isBind));

        appendUrl(URLDef.URL_CODE);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
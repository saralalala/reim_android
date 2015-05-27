package netUtils.request.user;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ChangePasswordRequest extends BaseRequest
{
    public ChangePasswordRequest(String oldPassword, String newPassword)
    {
        super();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("old_password", oldPassword));
        params.add(new BasicNameValuePair("new_password", newPassword));
        setParams(params);

        appendUrl(URLDef.URL_USER);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}
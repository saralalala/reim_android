package netUtils.request.user;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import classes.utils.AppPreference;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class DefaultManagerRequest extends BaseRequest
{
    public DefaultManagerRequest(int defaultManagerID)
    {
        super();

        String phone = AppPreference.getAppPreference().getCurrentUser().getPhone();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("manager_id", Integer.toString(defaultManagerID)));
        params.add(new BasicNameValuePair("phone", phone));
        setParams(params);

        appendUrl(URLDef.URL_USER);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}

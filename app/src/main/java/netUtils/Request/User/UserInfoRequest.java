package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

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

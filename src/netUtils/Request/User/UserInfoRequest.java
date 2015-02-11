package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.Request.BaseRequest;

public class UserInfoRequest extends BaseRequest
{
	public UserInfoRequest(int userID)
	{
		super();

		appendUrl(URLDef.URL_USER + "/" + userID);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}

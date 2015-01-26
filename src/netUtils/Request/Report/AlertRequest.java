package netUtils.Request.Report;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class AlertRequest extends BaseRequest
{
	public AlertRequest(int userID, int reportID)
	{
		super();

		appendUrl("/alert/" + userID + "/" + reportID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
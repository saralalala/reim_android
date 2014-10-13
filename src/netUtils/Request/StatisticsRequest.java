package netUtils.Request;

public class StatisticsRequest extends BaseRequest
{

	public StatisticsRequest()
	{
		super();
		String requestUrl = getUrl();
		requestUrl += "/stat";
		setUrl(requestUrl);
	}

	@Override
	public void sendRequest(HttpConnectionCallback callback)
	{
		// TODO Auto-generated method stub
		doGet(callback);
	}

}

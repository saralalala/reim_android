package netUtils.Request;

public class DownloadImageRequest extends BaseRequest
{
	public DownloadImageRequest(String url)
	{
		super();
		
		String requestUrl = getUrl();
		requestUrl += url;
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDownloadBinary(callback);
	}
}
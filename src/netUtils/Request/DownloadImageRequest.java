package netUtils.Request;

public class DownloadImageRequest extends BaseRequest
{
	public DownloadImageRequest(String url)
	{
		super();
		
		setUrl(url);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDownloadBinary(callback);
	}
}

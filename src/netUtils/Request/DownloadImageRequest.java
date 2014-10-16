package netUtils.Request;

import netUtils.HttpConnectionCallback;

public class DownloadImageRequest extends BaseRequest
{
	public DownloadImageRequest(String url)
	{
		super();
		
		String requestUrl = getUrl();
		requestUrl += url;
		setUrl(requestUrl);
	}
	
	public DownloadImageRequest(int imageID)
	{
		super();
		
		String requestUrl = getUrl();
		requestUrl += "/images/" + imageID;
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDownloadBinary(callback);
	}
}
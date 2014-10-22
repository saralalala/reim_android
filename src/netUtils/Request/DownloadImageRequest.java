package netUtils.Request;

import netUtils.HttpConnectionCallback;

public class DownloadImageRequest extends BaseRequest
{
	public DownloadImageRequest(String url)
	{
		super();
		
		setUrl(url);
	}
	
	public DownloadImageRequest(int imageID)
	{
		super();
		
		String requestUrl = getUrl();
		requestUrl += "/images/" + imageID + "/0";
		setUrl(requestUrl);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDownloadBinary(callback);
	}
}
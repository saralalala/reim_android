package netUtils.Request;

import netUtils.HttpConnectionCallback;

public class DownloadImageRequest extends BaseRequest
{
	public static final int IMAGE_QUALITY_VERY_HIGH = 0;
	public static final int IMAGE_QUALITY_LOW = 1;
	public static final int IMAGE_QUALITY_MEDIUM = 2;
	public static final int IMAGE_QUALITY_HIGH = 3;
	public static final int INVOICE_QUALITY_ORIGINAL = 4;
	
	public DownloadImageRequest(String url)
	{
		super();
		
		setUrl(url);
	}
	
	public DownloadImageRequest(int imageID, int type)
	{
		super();
		
		String urlSuffix = "/images/" + imageID + "/" + type;
		appendUrl(urlSuffix);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDownloadBinary(callback);
	}
}
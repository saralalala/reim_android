package netUtils.Request;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class UploadImageRequest extends BaseRequest
{
	public UploadImageRequest(String path, int type)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", "filePath"));
		params.add(new BasicNameValuePair("filePath", path));
		params.add(new BasicNameValuePair("type", Integer.toString(type)));
		setParams(params);

		setUrl("/images");
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}

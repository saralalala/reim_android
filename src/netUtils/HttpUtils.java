package netUtils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONException;
import org.json.JSONObject;

import classes.utils.AppPreference;

public class HttpUtils
{
	public static HttpClient getHttpClient()
	{
		HttpParams httpParams=new BasicHttpParams();
		
		HttpConnectionParams.setConnectionTimeout(httpParams, 20*1000);
		HttpConnectionParams.setSoTimeout(httpParams, 20*1000);
		HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
		
		HttpClientParams.setRedirecting(httpParams, true);
		
		HttpProtocolParams.setUserAgent(httpParams, NetworkConstant.USER_AGENT);
		HttpClient client=new DefaultHttpClient(httpParams);
		
		return client;
	}
	
	public static String getJWTString()
	{
		try
		{
			AppPreference appPreference = AppPreference.getAppPreference();
			JSONObject jObject = new JSONObject();
			jObject.put(NetworkConstant.USERNAME, appPreference.getUsername());
			jObject.put(NetworkConstant.PASSWORD, appPreference.getPassword());
			jObject.put(NetworkConstant.DEVICE_TYPE, NetworkConstant.DEVICE_TYPE_ANDROID);
			jObject.put(NetworkConstant.DEVICE_TOKEN, appPreference.getDeviceToken());
			jObject.put(NetworkConstant.SERVER_TOKEN, appPreference.getServerToken());
			String resultString = jObject.toString();

			return ReimJWT.Encode(resultString);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return "";
		}		
	}
}
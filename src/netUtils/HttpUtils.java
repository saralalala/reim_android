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

import classes.Utils.AppPreference;

public class HttpUtils
{
	public static HttpClient getHttpClient()
	{
		HttpParams httpParams=new BasicHttpParams();
		
		HttpConnectionParams.setConnectionTimeout(httpParams, 20*1000);
		HttpConnectionParams.setSoTimeout(httpParams, 20*1000);
		HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
		
		HttpClientParams.setRedirecting(httpParams, true);
		
		HttpProtocolParams.setUserAgent(httpParams, HttpConstant.USER_AGENT);
		HttpClient client=new DefaultHttpClient(httpParams);
		
		return client;
	}
	
	public static String getJWTString()
	{
		try
		{
			AppPreference appPreference = AppPreference.getAppPreference();
			JSONObject jObject = new JSONObject();
			jObject.put(HttpConstant.USERNAME, appPreference.getUsername());
			jObject.put(HttpConstant.PASSWORD, appPreference.getPassword());
			jObject.put(HttpConstant.DEVICE_TYPE, HttpConstant.DEVICE_TYPE_ANDROID);
			jObject.put(HttpConstant.DEVICE_TOKEN, appPreference.getDeviceToken());
			jObject.put(HttpConstant.SERVER_TOKEN, appPreference.getServerToken());
			String resultString=jObject.toString();
			
			return ReimJWT.Encode(resultString);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return "";
		}		
	}
}

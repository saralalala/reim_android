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
import classes.utils.PhoneUtils;

public class HttpUtils
{
	public static HttpClient getHttpClient()
	{
		HttpParams httpParams = new BasicHttpParams();
		
		HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);
		HttpConnectionParams.setSoTimeout(httpParams, 20 * 1000);
		HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
		
		HttpClientParams.setRedirecting(httpParams, true);
		
		HttpProtocolParams.setUserAgent(httpParams, getUserAgent());
		HttpClient client=new DefaultHttpClient(httpParams);
		
		return client;
	}

    public static HttpClient getHttpClient(int connectTimeout, int socketTimeout)
    {
        HttpParams httpParams = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(httpParams, connectTimeout * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, socketTimeout * 1000);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

        HttpClientParams.setRedirecting(httpParams, true);

        HttpProtocolParams.setUserAgent(httpParams, getUserAgent());
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
	
	private static String getUserAgent()
	{
		String result = NetworkConstant.USER_AGENT + ",";
		result += NetworkConstant.DEVICE_TYPE_ANDROID + ",";
		result += PhoneUtils.getAppVersion() + ",";
		result += AppPreference.getAppPreference().getUsername();
		return result;
	}
}
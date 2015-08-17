package netUtils.common;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyStore;

import classes.utils.AppPreference;
import classes.utils.PhoneUtils;

@SuppressWarnings("deprecation")
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
        return wrapClient(httpParams);
    }

    public static HttpClient getHttpClient(int connectTimeout, int socketTimeout)
    {
        HttpParams httpParams = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(httpParams, connectTimeout * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, socketTimeout * 1000);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

        HttpClientParams.setRedirecting(httpParams, true);

        HttpProtocolParams.setUserAgent(httpParams, getUserAgent());

        return wrapClient(httpParams);
    }

    public static HttpClient wrapClient(HttpParams params)
    {
        try
        {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new ReimSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            return new DefaultHttpClient(ccm, params);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return new DefaultHttpClient(params);
        }
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
            if (appPreference.isProxyMode())
            {
                jObject.put(NetworkConstant.PROXY, appPreference.getCurrentUserID());
            }
            String resultString = jObject.toString();

            return ReimJWT.Encode(resultString);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static NameValuePair getUberTokenHeader(String token)
    {
        return new BasicNameValuePair("Authorization", "Bearer " + token);
    }

    private static String getUserAgent()
    {
        String result = NetworkConstant.USER_AGENT + ",";
        result += NetworkConstant.DEVICE_TYPE_ANDROID + ",";
        result += PhoneUtils.getAppVersion() + ",";
        result += AppPreference.getAppPreference().getUsername() + ",";
        result += PhoneUtils.getPhoneModel() + ",";
        result += PhoneUtils.getSystemVersion();
        return result;
    }
}
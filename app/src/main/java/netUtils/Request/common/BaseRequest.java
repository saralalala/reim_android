package netUtils.request.common;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.rushucloud.reim.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import classes.utils.AppPreference;
import classes.utils.ViewUtils;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.HttpUtils;
import netUtils.common.NetworkConstant;
import netUtils.common.URLDef;

@SuppressWarnings("deprecation")
public abstract class BaseRequest
{
    private String url;
    private List<NameValuePair> headers = new ArrayList<>();
    private List<NameValuePair> params = new ArrayList<>();
    private HttpClient httpClient;

    protected BaseRequest()
    {
        this.httpClient = HttpUtils.getHttpClient();
        this.url = AppPreference.getAppPreference().isSandboxMode() ? URLDef.URL_PREFIX_SANDBOX : URLDef.URL_PREFIX;
    }

    protected BaseRequest(int connectTimeout, int socketTimeout)
    {
        this.httpClient = HttpUtils.getHttpClient(connectTimeout, socketTimeout);
        this.url = URLDef.URL_PREFIX;
    }

    protected void setUrl(String url)
    {
        this.url = url;
    }

    protected void appendUrl(String suffix)
    {
        this.url += "/" + suffix;
    }

    protected void appendUrl(int suffix)
    {
        this.url += "/" + suffix;
    }

    protected void appendUrl(long suffix)
    {
        this.url += "/" + suffix;
    }

    protected void addHeaders(NameValuePair nameValuePair)
    {
        this.headers.add(nameValuePair);
    }

    protected void addParams(String key, String value)
    {
        this.params.add(new BasicNameValuePair(key, value));
    }

    protected void addParams(String key, int value)
    {
        this.params.add(new BasicNameValuePair(key, Integer.toString(value)));
    }

    protected void addParams(String key, double value)
    {
        this.params.add(new BasicNameValuePair(key, Double.toString(value)));
    }

    public abstract void sendRequest(HttpConnectionCallback callback);

    protected void doPost(HttpConnectionCallback callback)
    {
        try
        {
            HttpPost request = new HttpPost(url);

            if (url.contains("images"))
            {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                File compressedBitmapFile = new File(params.get(1).getValue());

                builder.addBinaryBody(params.get(1).getName(), compressedBitmapFile);
                builder.addTextBody(params.get(0).getName(), params.get(0).getValue());
                builder.addTextBody(params.get(2).getName(), params.get(2).getValue());
                request.setEntity(builder.build());
            }
            else if (params != null)
            {
                request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            }

            doRequest(request, callback);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    protected void doPut(HttpConnectionCallback callback)
    {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8);
        StringBody stringBody;
        for (int i = 0; i < params.size(); i++)
        {
            stringBody = new StringBody(params.get(i).getValue(), contentType);
            builder.addPart(params.get(i).getName(), stringBody);
        }

        HttpPut request = new HttpPut(url);
        request.setEntity(builder.build());

        doRequest(request, callback);
    }

    protected void doGet(HttpConnectionCallback callback)
    {
        if (params != null)
        {
            List<String> paramsList = new ArrayList<>();
            for (NameValuePair param : params)
            {
                try
                {
                    String parameter = param.getName() + "=" + URLEncoder.encode(param.getValue(), "UTF-8");
                    paramsList.add(parameter);
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
            }

            url += "?" + TextUtils.join("&", paramsList);
        }

        HttpGet request = new HttpGet(url);
        doRequest(request, callback);
    }

    protected void doDelete(HttpConnectionCallback callback)
    {
        if (params != null)
        {
            List<String> paramsList = new ArrayList<>();
            for (NameValuePair param : params)
            {
                try
                {
                    String parameter = param.getName() + "=" + URLEncoder.encode(param.getValue(), "UTF-8");
                    paramsList.add(parameter);
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
            }

            url += "?" + TextUtils.join("&", paramsList);
        }

        HttpDelete request = new HttpDelete(url);
        doRequest(request, callback);
    }

    protected void doDownloadBinary(final HttpConnectionCallback callback)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                InputStream inputStream = null;

                try
                {
                    HttpGet request = new HttpGet(url);
                    request.addHeader(NetworkConstant.X_REIM_JWT, HttpUtils.getJWTString());

                    HttpResponse response = httpClient.execute(request);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                    {
                        inputStream = response.getEntity().getContent();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                callback.execute(inputStream);
            }
        }).start();
    }

    private void doRequest(final HttpUriRequest request, final HttpConnectionCallback callback)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                String resultString;
                try
                {
                    request.addHeader(NetworkConstant.X_REIM_JWT, HttpUtils.getJWTString());
                    for (NameValuePair header : headers)
                    {
                        request.addHeader(header.getName(), header.getValue());
                    }

                    HttpResponse response = httpClient.execute(request);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                    {
                        resultString = EntityUtils.toString(response.getEntity());
                    }
                    else
                    {
                        resultString = response.getStatusLine().getReasonPhrase();
                    }
                }
                catch (Exception e)
                {
                    resultString = constructNetworkError();
                    e.printStackTrace();
                }

                if (callback != null)
                {
                    callback.execute(resultString);
                }
            }
        }).start();
    }

    private String constructNetworkError()
    {
        JSONObject dataObject = new JSONObject();
        dataObject.put("msg", ViewUtils.getString(R.string.error_network_internet_unavailable));

        JSONObject object = new JSONObject();
        object.put("status", 0);
        object.put("code", -1);
        object.put("server_token", "");
        object.put("data", dataObject);

        return object.toString();
    }
}
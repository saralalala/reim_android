package netUtils.Request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.ReimJWT;
import netUtils.URLDef;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import classes.AppPreference;

public abstract class BaseRequest
{
	private String url;
	private List<NameValuePair> params;
	private HttpClient httpClient;

	protected BaseRequest()
	{
		this.httpClient = getHttpClient();
		this.url = URLDef.URL_PREFIX;
		params = null;
	}
	
	protected String getUrl()
	{
		return url;
	}

	protected void setUrl(String url)
	{
		this.url = url;
	}
	
	protected void appendUrl(String suffix)
	{
		this.url += suffix;
	}
	
	protected void setParams(List<NameValuePair> params)
	{
		this.params = new ArrayList<NameValuePair>(params);
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
			else
			{
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			}
			
			doRequest(request, callback);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return;
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
			List<String> paramsList = new ArrayList<String>();
			Iterator<NameValuePair> it = params.iterator();
			while (it.hasNext())
			{
				try
				{
					NameValuePair pair = it.next();
					String parameter = pair.getName() + "=" + URLEncoder.encode(pair.getValue(), "UTF-8");
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
			List<String> paramsList = new ArrayList<String>();
			Iterator<NameValuePair> it = params.iterator();
			while (it.hasNext())
			{
				try
				{
					NameValuePair pair = it.next();
					String parameter = pair.getName() + "=" + URLEncoder.encode(pair.getValue(), "UTF-8");
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
				String resultString = null;
				InputStream inputStream = null;
			
				try
				{
					HttpGet request = new HttpGet(url);	
					request.addHeader(HttpConstant.X_REIM_JWT, getJWTString());

					HttpResponse response = httpClient.execute(request);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
					{
						inputStream = response.getEntity().getContent();
					}
					else
					{
						resultString = response.getStatusLine().getReasonPhrase();
					}
				} 
				catch (ClientProtocolException e)
				{
					e.printStackTrace();
					System.out.println(resultString);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					System.out.println(resultString);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.out.println(resultString);
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
				String resultString = null;
				try
				{
					request.addHeader(HttpConstant.X_REIM_JWT, getJWTString());
					
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
				catch (ClientProtocolException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				if (callback != null)
				{
					callback.execute(resultString);					
				}
			}
		}).start();
	}

	private HttpClient getHttpClient()
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
	
	private String getJWTString()
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
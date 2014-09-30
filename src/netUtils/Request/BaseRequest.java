package netUtils.Request;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import classes.AppPreference;

public abstract class BaseRequest
{
	private String url;
	private List<NameValuePair> params;
	private HttpClient httpClient;

	protected BaseRequest()
	{
		this.httpClient = getHttpClient();
		this.url = URLDef.TEST_URL_PREFIX;
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
	
	protected void setParams(List<NameValuePair> params)
	{
		this.params = new ArrayList<NameValuePair>(params);
	}
	
	public abstract void sendRequest(HttpConnectionCallback callback);
	
	public interface HttpConnectionCallback
	{
		void execute(Object httpResponse);
	}
	
	protected void doPost(HttpConnectionCallback callback)
	{
		try
		{
			HttpPost request = new HttpPost(url);
			
			if (url.contains("images"))
			{
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

				AppPreference appPreference = AppPreference.getAppPreference();

				Matrix matrix = new Matrix();
				matrix.postScale((float)0.5, (float)0.5);
				
				Bitmap bitmap = BitmapFactory.decodeFile(params.get(1).getValue());
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				
				File compressedBitmapFile = new File(appPreference.getCacheDirectory(), "compressedBitmapFile");
				compressedBitmapFile.createNewFile();
				
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.JPEG, 90, outputStream);
				byte[] bitmapData = outputStream.toByteArray();
				
				FileOutputStream fileOutputStream = new FileOutputStream(compressedBitmapFile);
				fileOutputStream.write(bitmapData);
				fileOutputStream.flush();
				fileOutputStream.close();
				
				builder.addBinaryBody(params.get(1).getName(), compressedBitmapFile);
				builder.addTextBody(params.get(0).getName(), params.get(0).getValue());	
				builder.addTextBody(params.get(2).getName(), params.get(2).getValue());
				request.setEntity(builder.build());
			}
			else
			{
				request.setEntity(new UrlEncodedFormEntity(params));
			}
			
			doRequest(request, callback);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	protected void doPut(HttpConnectionCallback callback)
	{
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		for (int i = 0; i < params.size(); i++)
		{
			builder.addTextBody(params.get(i).getName(), params.get(i).getValue());		
		}
		
		HttpPut request = new HttpPut(url);
		request.setEntity(builder.build());

		doRequest(request, callback);
	}

	protected void doGet(HttpConnectionCallback callback)
	{
		if (url.indexOf("?") < 0)
		{
			url += "?";
		}
		if (params != null)
		{
			Iterator<NameValuePair> it = params.iterator();
			while (it.hasNext())
			{
				try
				{
					NameValuePair pair = it.next();
					url += "&" + pair.getName() + "=" + URLEncoder.encode(pair.getValue(), "UTF-8");
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
			}
			
			int index = url.indexOf("&");
			if (index != -1)
			{
				url = url.substring(0, index) + url.substring(index+1);
			}
		}
		else
		{
			url = url.substring(0, url.length()-1);
		}

		HttpGet request = new HttpGet(url);		
		doRequest(request, callback);
	}

	protected void doDelete(HttpConnectionCallback callback)
	{
		if (url.indexOf("?") < 0)
		{
			url += "?";
		}
		if (params != null)
		{
			Iterator<NameValuePair> it = params.iterator();
			while (it.hasNext())
			{
				try
				{
					url += "&" + it.next().getName() + "=" + URLEncoder.encode(it.next().getValue(), "UTF-8");
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
			}
			
			int index = url.indexOf("&");
			if (index != -1)
			{
				url = url.substring(0, index) + url.substring(index+1);
			}
		}
		else
		{
			url = url.substring(0, url.length()-1);
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
					resultString = e.getMessage().toString();
					System.out.println(resultString);
				}
				catch (IOException e)
				{
					resultString = e.getMessage().toString();
					System.out.println(resultString);
				}
				catch (Exception e)
				{
					resultString = e.getMessage().toString();
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
					resultString = e.getMessage().toString();
					System.out.println(resultString);
				}
				catch (IOException e)
				{
					resultString = e.getMessage().toString();
					System.out.println(resultString);
				}
				catch (Exception e)
				{
					resultString = e.getMessage().toString();
					System.out.println(resultString);
				}

				callback.execute(resultString);
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
//			jObject.put(HttpConstant.USERNAME, appPreference.getUsername());
//			jObject.put(HttpConstant.PASSWORD, appPreference.getPassword());
			jObject.put(HttpConstant.USERNAME, HttpConstant.DEBUG_EMAIL);
			jObject.put(HttpConstant.PASSWORD, HttpConstant.DEBUG_PASSWORD);
			jObject.put(HttpConstant.DEVICE_TYPE, HttpConstant.DEVICE_TYPE_ANDROID);
			jObject.put(HttpConstant.DEVICE_TOKEN, appPreference.getDeviceToken());
			//jObject.put(HttpConstant.SERVER_TOKEN, userInfo.getServerToken());
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
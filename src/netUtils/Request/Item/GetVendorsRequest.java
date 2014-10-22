package netUtils.Request.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class GetVendorsRequest extends BaseRequest
{
	public GetVendorsRequest(String category, double latitude, double longitude)
	{
		String appKey = "33331153";
		String secret = "f1ab2e9ab04a4959a1bf8cb5740bb598";
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("format", "json");
		paramMap.put("category", category);
		paramMap.put("latitude", Double.toString(latitude));  
		paramMap.put("longitude", Double.toString(longitude));
		paramMap.put("sort", "1");
		paramMap.put("limit", "20");
		paramMap.put("offset_type", "1");
		paramMap.put("out_offset_type", "1");
		paramMap.put("platform", "2");
		
		String[] keyArray = paramMap.keySet().toArray(new String[0]);
		Arrays.sort(keyArray);
		
		StringBuilder builder = new StringBuilder();
		builder.append(appKey);
		for (String key : keyArray)
		{
			builder.append(key).append(paramMap.get(key));
		}
		builder.append(secret);
		
		String codes = builder.toString();
		String sign = new String(Hex.encodeHex(DigestUtils.sha1(codes))).toUpperCase(Locale.getDefault());
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("appkey", appKey));
		params.add(new BasicNameValuePair("sign", sign));
		for (int i = 0; i < paramMap.size(); i++)
		{
			String key = keyArray[i];
			params.add(new BasicNameValuePair(key, paramMap.get(key)));		
		}
		setParams(params);
		
		String requestUrl = "http://api.dianping.com/v1/business/find_businesses";
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
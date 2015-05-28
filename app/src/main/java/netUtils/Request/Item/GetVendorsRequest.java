package netUtils.request.item;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class GetVendorsRequest extends BaseRequest
{
    private static final String appKey = "33331153";
    private static final String secret = "f1ab2e9ab04a4959a1bf8cb5740bb598";

    public GetVendorsRequest(double latitude, double longitude)
    {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("format", "json");
        paramMap.put("latitude", Double.toString(latitude));
        paramMap.put("longitude", Double.toString(longitude));
        paramMap.put("sort", "1");
        paramMap.put("limit", "20");
        paramMap.put("offset_type", "1");
        paramMap.put("out_offset_type", "1");
        paramMap.put("platform", "2");
//		if (!category.isEmpty())
//		{
//			paramMap.put("category", category);			
//		}

        Set<String> keySet = paramMap.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
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

        addParams("appkey", appKey);
        addParams("sign", sign);
        for (int i = 0; i < paramMap.size(); i++)
        {
            String key = keyArray[i];
            addParams(key, paramMap.get(key));
        }

        setUrl(URLDef.URL_DIANPING);
    }

    public GetVendorsRequest(String city, String keyword)
    {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("format", "json");
        paramMap.put("city", city);
        paramMap.put("keyword", keyword);
        paramMap.put("sort", "1");
        paramMap.put("limit", "20");
        paramMap.put("out_offset_type", "1");
        paramMap.put("platform", "2");

        Set<String> keySet = paramMap.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
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

        addParams("appkey", appKey);
        addParams("sign", sign);
        for (int i = 0; i < paramMap.size(); i++)
        {
            String key = keyArray[i];
            addParams(key, paramMap.get(key));
        }

        setUrl(URLDef.URL_DIANPING);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
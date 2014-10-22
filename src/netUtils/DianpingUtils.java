package netUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

public class DianpingUtils
{

    /**
     * 获取请求字符串
     * 
     * @param appKey
     * @param secret
     * @param paramMap
     * @return
     */
    public static String getQueryString(String appKey, String secret, Map<String, String> paramMap)
    {
        String sign = sign(appKey, secret, paramMap);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("appkey=").append(appKey).append("&sign=").append(sign);
        for (Entry<String, String> entry : paramMap.entrySet())
        {
            stringBuilder.append('&').append(entry.getKey()).append('=').append(entry.getValue());
        }
        String queryString = stringBuilder.toString();
        return queryString;
    }

    /**
     * 获取请求字符串，参数值进行UTF-8处理
     * 
     * @param appKey
     * @param secret
     * @param paramMap
     * @return
     */
    public static String getUrlEncodedQueryString(String appKey, String secret, Map<String, String> paramMap)
    {
        String sign = sign(appKey, secret, paramMap);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("appkey=").append(appKey).append("&sign=").append(sign);
        for (Entry<String, String> entry : paramMap.entrySet())
        {
            try
            {
                stringBuilder.append('&').append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(),
                                                                                                      "UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
            }
        }
        String queryString = stringBuilder.toString();
        return queryString;
    }

    /**
     * 签名
     * 
     * @param appKey
     * @param secret
     * @param paramMap
     * @return
     */
    public static String sign(String appKey, String secret, Map<String, String> paramMap)
    {
        // 参数名排序
        String[] keyArray = paramMap.keySet().toArray(new String[0]);
        Arrays.sort(keyArray);

        // 拼接参数
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(appKey);
        for (String key : keyArray)
        {
            stringBuilder.append(key).append(paramMap.get(key));
        }

        stringBuilder.append(secret);
        String codes = stringBuilder.toString();

        // SHA-1签名
        // For Android
        String sign = new String(Hex.encodeHex(DigestUtils.sha1(codes))).toUpperCase();

        return sign;
    }
}
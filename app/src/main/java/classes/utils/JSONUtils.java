package classes.utils;

import com.alibaba.fastjson.JSONObject;

public class JSONUtils
{
    public static int optInt(JSONObject object, String key, int defaultValue)
    {
        Integer result = object.getInteger(key);
        return result == null ? defaultValue : result;
    }

    public static double optDouble(JSONObject object, String key, double defaultValue)
    {
        Double result = object.getDouble(key);
        return result == null ? defaultValue : result;
    }

    public static String optString(JSONObject object, String key, String defaultValue)
    {
        String result = object.getString(key);
        return result == null ? defaultValue : result;
    }
}

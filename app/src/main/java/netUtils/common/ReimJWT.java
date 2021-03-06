package netUtils.common;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ReimJWT
{
    private final static String PUBKEY = "1NDgzZGY1OWViOWRmNjI5ZT";
    private static final String ALGORITHM = "hmacSHA256";

    public static String Encode(String jsonPayload)
    {
        JSONObject header = new JSONObject();
        String headerPart, bodyPart;
        try
        {
            header.put("typ", "JWT");
            header.put("alg", "HS256");

            String headerStr = header.toString();
            headerPart = ReimJWT.reimJWTReplace(Base64.encodeToString(headerStr.getBytes(), 1));
            bodyPart = ReimJWT.reimJWTReplace(Base64.encodeToString(jsonPayload.getBytes(), 1));
            String signPart = headerPart + "." + bodyPart;
            String signString = reimJWTReplace(sign(signPart));
            String result = signPart + "." + signString;
            return result;
        }
        catch (JSONException ex)
        {
            return "";
        }
    }

    private static String sign(String inputData)
    {
        String temp = null;
        SecretKeySpec keySpec = new SecretKeySpec(PUBKEY.getBytes(), ALGORITHM);
        try
        {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(keySpec);
            mac.update(inputData.getBytes());
            byte[] m = mac.doFinal();
            temp = Base64.encodeToString(m, Base64.DEFAULT);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return temp;
    }

    private static String reimJWTReplace(String seed)
    {
        String seedOut = seed.replaceAll("/", "_").replaceAll("\\+", "-").replaceAll("=", "").replaceAll("\\n", "");
        return seedOut;
    }
}

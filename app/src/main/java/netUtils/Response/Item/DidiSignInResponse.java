package netUtils.response.item;

import org.json.JSONException;
import org.json.JSONObject;

public class DidiSignInResponse
{
    private boolean status;
    private String token;

    public DidiSignInResponse(Object httpResponse)
    {
        try
        {
            JSONObject jObject = new JSONObject((String) httpResponse);
            status = jObject.getInt("errno") == 0;
            token = jObject.optString("token", "");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            status = false;
        }
    }

    public boolean getStatus()
    {
        return status;
    }

    public String getToken()
    {
        return token;
    }
}
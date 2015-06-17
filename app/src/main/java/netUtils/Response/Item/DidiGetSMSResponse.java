package netUtils.response.item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Vendor;

public class DidiGetSMSResponse
{
    private boolean status;

    public DidiGetSMSResponse(Object httpResponse)
    {
        try
        {
            JSONObject jObject = new JSONObject((String) httpResponse);
            status = jObject.getInt("errno") == 0;
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
}
package netUtils.response.item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.utils.Utils;
import netUtils.response.BaseResponse;

public class ModifyItemResponse extends BaseResponse
{
    private int itemID;

    public ModifyItemResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONArray jsonArray = getDataArray();
            JSONObject jObject = jsonArray.getJSONObject(0);
            setStatus(Utils.intToBoolean(jObject.getInt("status")));
            if (getStatus())
            {
                setItemID(Integer.valueOf(jObject.getString("iid")));
            }
            else
            {
                setErrorMessage(jObject.getString("msg"));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getItemID()
    {
        return itemID;
    }

    public void setItemID(int itemID)
    {
        this.itemID = itemID;
    }
}

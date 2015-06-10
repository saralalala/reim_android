package netUtils.response.item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.utils.Utils;
import netUtils.response.common.BaseResponse;

public class CreateItemsResponse extends BaseResponse
{
    private int itemID;

    public CreateItemsResponse(Object httpResponse)
    {
        super(httpResponse);
        if (!getStatus())
        {
            constructErrorData();
        }
    }

    protected void constructData()
    {
        try
        {
            JSONArray jsonArray = getDataArray();
            JSONObject jObject = jsonArray.getJSONObject(0);
            setItemID(Integer.valueOf(jObject.getString("id")));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    protected void constructErrorData()
    {
        try
        {
            JSONArray jsonArray = getDataArray();
            JSONObject jObject = jsonArray.getJSONObject(0);
            setStatus(Utils.intToBoolean(jObject.getInt("status")));
            setErrorMessage(jObject.getString("msg"));
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

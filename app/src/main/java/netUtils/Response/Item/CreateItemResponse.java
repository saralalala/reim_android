package netUtils.response.item;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import classes.utils.LogUtils;
import classes.utils.Utils;
import netUtils.response.common.BaseResponse;

public class CreateItemResponse extends BaseResponse
{
    private int itemID;
    private double rate;

    public CreateItemResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONArray jsonArray = getDataArray();
            JSONObject jObject = jsonArray.getJSONObject(0);
            setStatus(Utils.intToBoolean(jObject.getInteger("status")));
            if (getStatus())
            {
                itemID = jObject.getInteger("iid");
                rate = jObject.getDouble("rate");
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

    public double getRate()
    {
        return rate;
    }
}

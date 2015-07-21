package netUtils.response.item;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class DidiDetailLiftResponse
{
    private boolean status;
    private double amount = 0;
    private double latitude = 0;
    private double longitude = 0;

    public DidiDetailLiftResponse(Object httpResponse)
    {
        try
        {
            JSONObject jObject = JSON.parseObject((String) httpResponse);
            status = jObject.getInteger("errno") == 0;

            JSONObject orderObject = jObject.getJSONObject("order_info");
            amount = orderObject.getJSONObject("price_detail").getDouble("total_price");
            latitude = orderObject.getDouble("from_lat");
            longitude = orderObject.getDouble("from_lng");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            status = false;
        }
    }

    public boolean getStatus()
    {
        return status;
    }

    public double getAmount()
    {
        return amount;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }
}

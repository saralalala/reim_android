package netUtils.response.item;

import org.json.JSONException;
import org.json.JSONObject;

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
            JSONObject jObject = new JSONObject((String) httpResponse);
            status = jObject.getInt("errno") == 0;

            JSONObject orderObject = jObject.getJSONObject("order_info");
            amount = orderObject.getJSONObject("price_detail").getDouble("total_price");
            latitude = orderObject.getDouble("from_lat");
            longitude = orderObject.getDouble("from_lng");
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

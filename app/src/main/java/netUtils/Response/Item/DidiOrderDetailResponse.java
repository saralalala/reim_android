package netUtils.response.item;

import org.json.JSONException;
import org.json.JSONObject;

import classes.model.StatCategory;

public class DidiOrderDetailResponse
{
    private boolean status;
    private double amount = 0;

    public DidiOrderDetailResponse(Object httpResponse)
    {
        try
        {
            JSONObject jObject = new JSONObject((String) httpResponse);
            status = jObject.getInt("errno") == 0;
            amount = jObject.getJSONObject("coupon").getDouble("total_fee") / 100;
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
}

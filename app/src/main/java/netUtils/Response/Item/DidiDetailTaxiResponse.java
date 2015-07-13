package netUtils.response.item;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.rushucloud.reim.R;

import classes.utils.ViewUtils;

public class DidiDetailTaxiResponse
{
    private boolean status;
    private double amount = 0;
    private String city = "";

    public DidiDetailTaxiResponse(Object httpResponse)
    {
        try
        {
            JSONObject jObject = JSON.parseObject((String) httpResponse);
            status = jObject.getInteger("errno") == 0;
            amount = jObject.getJSONObject("coupon").getDouble("total_fee") / 100;
            city = jObject.getJSONObject("base").getString("city_name");
            int index = city.indexOf(ViewUtils.getString(R.string.city));
            if (index > 0)
            {
                city = city.substring(0, index);
            }
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

    public String getCity()
    {
        return city;
    }
}

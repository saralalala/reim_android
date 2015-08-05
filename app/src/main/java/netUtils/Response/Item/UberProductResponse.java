package netUtils.response.item;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rushucloud.reim.R;

import classes.utils.ViewUtils;

public class UberProductResponse
{
    private boolean status = true;
    private double amount = 0;

    public UberProductResponse(Object httpResponse)
    {
        try
        {
            JSONObject jObject = JSON.parseObject((String) httpResponse);
            amount = jObject.getJSONObject("feeInfo").getDouble("total_fee");
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
}

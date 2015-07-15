package netUtils.response.item;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Currency;
import netUtils.response.common.BaseResponse;

public class CurrencyResponse extends BaseResponse
{
    private List<Currency> currencyList;

    public CurrencyResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();

            currencyList = new ArrayList<>();
            for (String key : jObject.keySet())
            {
                double value = jObject.getDouble(key);

                Currency currency = new Currency();
                currency.setCode(key.toUpperCase());
                currency.setRate(value);

                currencyList.add(currency);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public List<Currency> getCurrencyList()
    {
        return currencyList;
    }
}

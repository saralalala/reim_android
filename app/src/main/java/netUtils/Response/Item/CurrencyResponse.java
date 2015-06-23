package netUtils.response.item;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
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
            Iterator iterator = jObject.keys();
            while (iterator.hasNext())
            {
                String key = (String) iterator.next();
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

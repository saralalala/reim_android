package netUtils.response.statistics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import classes.model.StatCategory;
import classes.model.StatTag;
import netUtils.response.common.BaseResponse;

public class MineStatDetailResponse extends BaseResponse
{
    private double totalAmount;
    private double newAmount;
    private List<StatCategory> statCategoryList;
    private HashMap<String, Double> monthsData;
    private List<StatTag> statTagList;
    private HashMap<String, Double> currencyData;

    public MineStatDetailResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();

            newAmount = jObject.getDouble("new");
            totalAmount = jObject.getDouble("done") + jObject.getDouble("process") + newAmount;

            monthsData = new HashMap<>();
            JSONObject months = jObject.optJSONObject("ms");
            if (months != null)
            {
                for (Iterator<?> iterator = months.keys(); iterator.hasNext(); )
                {
                    String key = (String) iterator.next();
                    Double value = months.getDouble(key);
                    monthsData.put(key, value);
                }
            }

            statCategoryList = new ArrayList<>();
            JSONArray categories = jObject.getJSONArray("cates");
            for (int i = 0; i < categories.length(); i++)
            {
                JSONObject object = categories.getJSONObject(i);
                statCategoryList.add(new StatCategory(object));
            }

            currencyData = new HashMap<>();
            JSONObject currencies = jObject.optJSONObject("currencies");
            if (currencies != null)
            {
                for (Iterator<?> iterator = currencies.keys(); iterator.hasNext(); )
                {
                    String key = (String) iterator.next();
                    Double value = currencies.getDouble(key);
                    currencyData.put(key.toUpperCase(), value);
                }
            }

            statTagList = new ArrayList<>();
            JSONArray tags = jObject.getJSONArray("tags");
            for (int i = 0; i < tags.length(); i++)
            {
                JSONObject object = tags.getJSONObject(i);
                statTagList.add(new StatTag(object));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public double getTotalAmount()
    {
        return totalAmount;
    }

    public double getNewAmount()
    {
        return newAmount;
    }

    public List<StatCategory> getStatCategoryList()
    {
        return statCategoryList;
    }

    public HashMap<String, Double> getMonthsData()
    {
        return monthsData;
    }

    public HashMap<String, Double> getCurrencyData()
    {
        return currencyData;
    }

    public List<StatTag> getStatTagList()
    {
        return statTagList;
    }
}
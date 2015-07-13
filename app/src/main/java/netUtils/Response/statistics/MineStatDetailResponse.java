package netUtils.response.statistics;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
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
            JSONObject months = jObject.getJSONObject("ms");
            if (months != null)
            {
                for (String key : months.keySet())
                {
                    Double value = months.getDouble(key);
                    monthsData.put(key, value);
                }
            }

            statCategoryList = new ArrayList<>();
            JSONArray categories = jObject.getJSONArray("cates");
            for (int i = 0; i < categories.size(); i++)
            {
                JSONObject object = categories.getJSONObject(i);
                statCategoryList.add(new StatCategory(object));
            }

            currencyData = new HashMap<>();
            JSONArray currencies = jObject.getJSONArray("currencies");
            if (currencies != null)
            {
                for (int i = 0; i < currencies.size(); i++)
                {
                    JSONObject object = currencies.getJSONObject(i);
                    currencyData.put(object.getString("name").toUpperCase(), object.getDouble("amount"));
                }
            }

            statTagList = new ArrayList<>();
            JSONArray tags = jObject.getJSONArray("tags");
            for (int i = 0; i < tags.size(); i++)
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
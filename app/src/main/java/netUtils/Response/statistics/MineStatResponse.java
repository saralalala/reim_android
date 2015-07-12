package netUtils.response.statistics;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import classes.model.StatCategory;
import classes.model.StatTag;
import classes.utils.Utils;
import netUtils.response.common.BaseResponse;

public class MineStatResponse extends BaseResponse
{
    private boolean hasStaffData;
    private double newAmount;
    private double ongoingAmount;
    private HashMap<String, Double> monthsData;
    private List<StatCategory> statCategoryList;
    private HashMap<String, Double> currencyData;
    private List<StatTag> statTagList;

    public MineStatResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();

            hasStaffData = Utils.intToBoolean(jObject.getInteger("staff"));
            ongoingAmount = jObject.getDouble("process");
            newAmount = jObject.getDouble("new");

            statCategoryList = new ArrayList<>();
            JSONArray categories = jObject.getJSONArray("cates");
            for (int i = 0; i < categories.size(); i++)
            {
                StatCategory category = new StatCategory(categories.getJSONObject(i));
                statCategoryList.add(category);
            }

            statTagList = new ArrayList<>();
            JSONArray tags = jObject.getJSONArray("tags");
            for (int i = 0; i < tags.size(); i++)
            {
                JSONObject object = tags.getJSONObject(i);
                statTagList.add(new StatTag(object));
            }

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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean hasStaffData()
    {
        return hasStaffData;
    }

    public double getNewAmount()
    {
        return newAmount;
    }

    public double getOngoingAmount()
    {
        return ongoingAmount;
    }

    public HashMap<String, Double> getMonthsData()
    {
        return monthsData;
    }

    public List<StatCategory> getStatCategoryList()
    {
        return statCategoryList;
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
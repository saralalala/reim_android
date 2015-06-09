package netUtils.response.statistics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import classes.model.StatCategory;
import classes.model.StatTag;
import classes.model.StatUser;
import netUtils.response.BaseResponse;

public class MineStatDetailResponse extends BaseResponse
{
    private double totalAmount;
    private double newAmount;
    private List<StatCategory> statCategoryList;
    private HashMap<String, Double> monthsData;
    private List<StatTag> statTagList;

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

            this.monthsData = new HashMap<>();
            JSONObject months = jObject.optJSONObject("ms");
            if (months != null)
            {
                for (Iterator<?> iterator = months.keys(); iterator.hasNext(); )
                {
                    String key = (String) iterator.next();
                    Double value = months.getDouble(key);
                    this.monthsData.put(key, value);
                }
            }

            this.statCategoryList = new ArrayList<>();
            JSONArray categories = jObject.getJSONArray("cates");
            for (int i = 0; i < categories.length(); i++)
            {
                JSONObject object = categories.getJSONObject(i);
                this.statCategoryList.add(new StatCategory(object));
            }

            this.statTagList = new ArrayList<>();
            JSONArray tags = jObject.getJSONArray("tags");
            for (int i = 0; i < tags.length(); i++)
            {
                JSONObject object = tags.getJSONObject(i);
                this.statTagList.add(new StatTag(object));
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

    public List<StatTag> getStatTagList()
    {
        return statTagList;
    }
}
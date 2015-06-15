package netUtils.response.statistics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    private List<StatCategory> statCategoryList;
    private List<StatTag> statTagList;
    private HashMap<String, Double> monthsData;

    public MineStatResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();

            this.hasStaffData = Utils.intToBoolean(jObject.getInt("staff"));
            this.ongoingAmount = jObject.getDouble("process");
            this.newAmount = jObject.getDouble("new");

            this.statCategoryList = new ArrayList<>();
            JSONArray categories = jObject.getJSONArray("cates");
            for (int i = 0; i < categories.length(); i++)
            {
                StatCategory category = new StatCategory(categories.getJSONObject(i));
                this.statCategoryList.add(category);
            }

            this.statTagList = new ArrayList<>();
            JSONArray tags = jObject.getJSONArray("tags");
            for (int i = 0; i < tags.length(); i++)
            {
                JSONObject object = tags.getJSONObject(i);
                this.statTagList.add(new StatTag(object));
            }

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

    public List<StatCategory> getStatCategoryList()
    {
        return statCategoryList;
    }

    public List<StatTag> getStatTagList()
    {
        return statTagList;
    }

    public HashMap<String, Double> getMonthsData()
    {
        return monthsData;
    }
}
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
import netUtils.response.common.BaseResponse;

public class OthersStatResponse extends BaseResponse
{
    private double totalAmount = 0;
    private List<StatCategory> statCategoryList;
    private HashMap<String, Double> statusData;
    private HashMap<String, Double> currencyData;
    private List<StatTag> statTagList;
    private List<StatUser> statUserList;

    public OthersStatResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();

            this.statCategoryList = new ArrayList<>();
            JSONArray categories = jObject.getJSONArray("categories");
            for (int i = 0; i < categories.length(); i++)
            {
                StatCategory category = new StatCategory(categories.getJSONObject(i));
                this.statCategoryList.add(category);
                this.totalAmount += category.getAmount();
            }

            this.statusData = new HashMap<>();
            JSONArray details = jObject.optJSONArray("detail");
            if (details != null)
            {
                for (int i = 0; i < details.length(); i++)
                {
                    JSONObject object = details.getJSONObject(i);
                    this.statusData.put(object.getString("desc"), object.getDouble("amount"));
                }
            }

            this.currencyData = new HashMap<>();
            JSONObject currencies = jObject.optJSONObject("currencies");
            if (currencies != null)
            {
                for (Iterator<?> iterator = currencies.keys(); iterator.hasNext(); )
                {
                    String key = (String) iterator.next();
                    Double value = currencies.getDouble(key);
                    this.currencyData.put(key.toUpperCase(), value);
                }
            }

            this.statTagList = new ArrayList<>();
            JSONArray tags = jObject.getJSONArray("tags");
            for (int i = 0; i < tags.length(); i++)
            {
                JSONObject object = tags.getJSONObject(i);
                this.statTagList.add(new StatTag(object));
            }

            this.statUserList = new ArrayList<>();
            JSONArray members = jObject.getJSONArray("members");
            for (int i = 0; i < members.length(); i++)
            {
                JSONObject object = members.getJSONObject(i);
                this.statUserList.add(new StatUser(object));
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

    public List<StatCategory> getStatCategoryList()
    {
        return statCategoryList;
    }

    public HashMap<String, Double> getStatusData()
    {
        return statusData;
    }

    public HashMap<String, Double> getCurrencyData()
    {
        return currencyData;
    }

    public List<StatTag> getStatTagList()
    {
        return statTagList;
    }

    public List<StatUser> getStatUserList()
    {
        return statUserList;
    }
}
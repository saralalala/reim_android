package netUtils.response.statistics;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import classes.model.StatCategory;
import classes.model.StatDepartment;
import classes.model.StatTag;
import classes.model.StatUser;
import netUtils.response.common.BaseResponse;

public class OthersStatResponse extends BaseResponse
{
    private double totalAmount;
    private List<StatCategory> statCategoryList;
    private HashMap<String, Double> statusData;
    private HashMap<String, Double> currencyData;
    private List<StatDepartment> statDepartmentList;
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

            statCategoryList = new ArrayList<>();
            JSONArray categories = jObject.getJSONArray("categories");
            for (int i = 0; i < categories.size(); i++)
            {
                StatCategory category = new StatCategory(categories.getJSONObject(i));
                statCategoryList.add(category);
                totalAmount += category.getAmount();
            }

            statusData = new HashMap<>();
            JSONArray details = jObject.getJSONArray("detail");
            if (details != null)
            {
                for (int i = 0; i < details.size(); i++)
                {
                    JSONObject object = details.getJSONObject(i);
                    statusData.put(object.getString("desc"), object.getDouble("val"));
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

            statDepartmentList = new ArrayList<>();
            JSONObject groupObject = jObject.getJSONObject("group");
            if (groupObject != null)
            {
                JSONArray groups = groupObject.getJSONArray("groups");
                for (int i = 0; i < groups.size(); i++)
                {
                    JSONObject object = groups.getJSONObject(i);
                    statDepartmentList.add(new StatDepartment(object, true));
                }

                JSONArray members = groupObject.getJSONArray("members");
                for (int i = 0; i < members.size(); i++)
                {
                    JSONObject object = members.getJSONObject(i);
                    statDepartmentList.add(new StatDepartment(object, false));
                }
            }

            statTagList = new ArrayList<>();
            JSONArray tags = jObject.getJSONArray("tags");
            for (int i = 0; i < tags.size(); i++)
            {
                JSONObject object = tags.getJSONObject(i);
                statTagList.add(new StatTag(object));
            }

            statUserList = new ArrayList<>();
            JSONArray members = jObject.getJSONArray("members");
            for (int i = 0; i < members.size(); i++)
            {
                JSONObject object = members.getJSONObject(i);
                statUserList.add(new StatUser(object));
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

    public List<StatDepartment> getStatDepartmentList()
    {
        return statDepartmentList;
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
package netUtils.response.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Item;
import classes.model.Report;
import classes.utils.LogUtils;

public class SyncDataResponse extends BaseResponse
{
    private List<Item> itemList;
    private List<Report> reportList;

    public SyncDataResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();

            reportList = new ArrayList<>();
            JSONArray jsonArray = jObject.getJSONArray("reports");
            for (int i = 0; i < jsonArray.size(); i++)
            {
                Report report = new Report(jsonArray.getJSONObject(i));
                reportList.add(report);
            }

            itemList = new ArrayList<>();
            jsonArray = jObject.getJSONArray("items");
            for (int i = 0; i < jsonArray.size(); i++)
            {
                Item item = new Item(jsonArray.getJSONObject(i));
                itemList.add(item);
            }
        }
        catch (JSONException e)
        {
            LogUtils.printError(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public List<Item> getItemList()
    {
        return itemList;
    }

    public List<Report> getReportList()
    {
        return reportList;
    }
}

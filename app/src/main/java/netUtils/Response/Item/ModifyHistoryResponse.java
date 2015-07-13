package netUtils.response.item;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

import java.util.ArrayList;
import java.util.List;

import classes.model.ModifyHistory;
import netUtils.response.common.BaseResponse;

public class ModifyHistoryResponse extends BaseResponse
{
    private List<ModifyHistory> historyList;

    public ModifyHistoryResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONArray jsonArray = getDataArray();

            historyList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++)
            {
                ModifyHistory info = new ModifyHistory(jsonArray.getJSONObject(i));
                historyList.add(info);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public List<ModifyHistory> getHistoryList()
    {
        return historyList;
    }
}
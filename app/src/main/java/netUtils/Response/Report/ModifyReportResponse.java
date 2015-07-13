package netUtils.response.report;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import netUtils.response.common.BaseResponse;

public class ModifyReportResponse extends BaseResponse
{
    private int reportID;

    public ModifyReportResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            reportID = Integer.valueOf(jObject.getString("id"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getReportID()
    {
        return reportID;
    }

    public List<Integer> getErrorCategoryIDList()
    {
        List<Integer> idList = new ArrayList<>();
        JSONObject quotaObject = getDataObject().getJSONObject("quota");
        for (String key : quotaObject.keySet())
        {
            int value = quotaObject.getInteger(key);
            if (value < 0)
            {
                idList.add(Integer.valueOf(key));
            }
        }
        return idList;
    }
}

package netUtils.response.report;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.utils.LogUtils;
import netUtils.response.common.BaseResponse;

public class CreateReportResponse extends BaseResponse
{
    private int reportID;

    public CreateReportResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            setReportID(Integer.valueOf(jObject.getString("id")));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public List<Integer> getErrorCategoryIDList()
    {
        List<Integer> idList = new ArrayList<>();
        JSONObject quotaObject = getDataObject().getJSONObject("quota");
        for (String key : quotaObject.keySet())
        {
            idList.add(Integer.valueOf(key));
        }
        return idList;
    }

    public int getReportID()
    {
        return reportID;
    }

    public void setReportID(int reportID)
    {
        this.reportID = reportID;
    }
}

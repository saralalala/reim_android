package netUtils.response.report;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

import classes.model.Report;
import netUtils.response.common.BaseResponse;

public class SingleSubReportResponse extends BaseResponse
{
    private List<Report> reportList;

    public SingleSubReportResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            JSONArray jsonArray = jObject.getJSONArray("data");
            int count = Integer.valueOf(jObject.getString("total"));
            for (int i = 0; i < count; i++)
            {
                JSONObject object = jsonArray.getJSONObject(i);
                Report report = new Report();
                report.setTitle(object.getString("title"));
                report.setCreatedDate(object.getInteger("createdt"));
//				report.setLastUpdatedDate(new Date(Integer.valueOf(object.getString("lastdt"))));
                report.setStatus(Integer.valueOf(object.getString("status")));
                reportList.add(report);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}

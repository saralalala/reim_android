package netUtils.response.report;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Report;
import classes.model.User;
import classes.utils.Utils;
import netUtils.response.common.BaseResponse;

public class SubordinatesReportResponse extends BaseResponse
{
    private List<Report> reportList;

    public SubordinatesReportResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            reportList = new ArrayList<>();
            JSONObject jObject = getDataObject();
            if (jObject != null)
            {
                JSONArray jsonArray = jObject.getJSONArray("data");
                int count = Integer.valueOf(jObject.getString("total"));
                for (int i = 0; i < count; i++)
                {
                    JSONObject object = jsonArray.getJSONObject(i);
                    Report report = new Report();
                    report.setTitle(object.getString("title"));
                    report.setServerID(object.getInteger("id"));
                    report.setStatus(Integer.valueOf(object.getString("status")));
                    report.setMyDecision(Integer.valueOf(object.getString("mdecision")));
                    report.setCreatedDate(object.getInteger("createdt"));
                    report.setServerUpdatedDate(object.getInteger("lastdt"));
                    report.setLocalUpdatedDate(object.getInteger("lastdt"));
                    report.setItemCount(object.getInteger("item_count"));
                    report.setAmount(object.getString("amount"));
                    report.setIsCC(Utils.intToBoolean(object.getInteger("cc_flag")));
                    report.setStep(object.getInteger("step"));

                    User user = new User();
                    user.setServerID(object.getInteger("uid"));
                    report.setSender(user);

                    reportList.add(report);
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public List<Report> getReportList()
    {
        return reportList;
    }
}
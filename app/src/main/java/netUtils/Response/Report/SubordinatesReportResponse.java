package netUtils.response.report;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Report;
import classes.model.User;
import classes.utils.Utils;
import netUtils.response.BaseResponse;

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
                    report.setServerID(object.getInt("id"));
                    report.setStatus(Integer.valueOf(object.getString("status")));
                    report.setMyDecision(Integer.valueOf(object.getString("mdecision")));
                    report.setCreatedDate(object.getInt("createdt"));
                    report.setServerUpdatedDate(object.getInt("lastdt"));
                    report.setLocalUpdatedDate(object.getInt("lastdt"));
                    report.setItemCount(object.getInt("item_count"));
                    report.setAmount(object.getString("amount"));
                    report.setIsCC(Utils.intToBoolean(object.getInt("cc_flag")));
                    report.setStep(object.getInt("step"));

                    User user = new User();
                    user.setServerID(object.getInt("uid"));
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
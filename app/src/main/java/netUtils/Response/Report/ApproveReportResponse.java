package netUtils.response.report;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.response.BaseResponse;

public class ApproveReportResponse extends BaseResponse
{
    private int reportID;
    private int reportStatus;

    public ApproveReportResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            setReportID(Integer.valueOf(jObject.getString("id")));
            setReportStatus(Integer.valueOf(jObject.getString("status")));
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

    public void setReportID(int reportID)
    {
        this.reportID = reportID;
    }

    public int getReportStatus()
    {
        return reportStatus;
    }

    public void setReportStatus(int reportStatus)
    {
        this.reportStatus = reportStatus;
    }
}

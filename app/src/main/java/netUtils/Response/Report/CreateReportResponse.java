package netUtils.response.report;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.response.BaseResponse;

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

    public int getReportID()
    {
        return reportID;
    }

    public void setReportID(int reportID)
    {
        this.reportID = reportID;
    }
}

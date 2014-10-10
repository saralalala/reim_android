package netUtils.Response.Report;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.Response.BaseResponse;

public class DeleteReportResponse extends BaseResponse
{
	private int reportID;
	
	public DeleteReportResponse(Object httpResponse)
	{
		super(httpResponse);
		if (getStatus())
		{
			constructData();
		}
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
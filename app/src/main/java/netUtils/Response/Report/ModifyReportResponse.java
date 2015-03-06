package netUtils.Response.Report;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.Response.BaseResponse;

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

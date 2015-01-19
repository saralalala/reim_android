package netUtils.Response.Report;

import org.json.JSONArray;

import netUtils.Response.BaseResponse;

public class ApproveInfoResponse extends BaseResponse
{	
	public ApproveInfoResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		JSONArray jsonArray = getDataArray();
		System.out.println(jsonArray.toString());
	}
}
package netUtils.Response;

public class FeedbackResponse extends BaseResponse
{
	public FeedbackResponse(Object httpResponse)
	{
		super(httpResponse);
		if (getStatus())
		{
			constructData();
		}
	}

	protected void constructData()
	{
		
	}
}

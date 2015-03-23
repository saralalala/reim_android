package netUtils.Response.User;

import classes.Message;
import netUtils.Response.BaseResponse;

public class GetMessageResponse extends BaseResponse
{
	private Message message;

	public GetMessageResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
        message = new Message(getDataObject());
	}

    public Message getMessage()
    {
        return message;
    }
}

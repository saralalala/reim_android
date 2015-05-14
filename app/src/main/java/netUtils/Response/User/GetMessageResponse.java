package netUtils.response.user;

import classes.model.Message;
import netUtils.response.BaseResponse;

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

package netUtils.Response.User;

import com.rushucloud.reim.R;

import org.json.JSONException;
import org.json.JSONObject;

import classes.Message;
import classes.utils.ReimApplication;
import classes.utils.Utils;
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

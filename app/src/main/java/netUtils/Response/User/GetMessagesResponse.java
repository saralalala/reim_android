package netUtils.response.user;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.Apply;
import classes.Invite;
import classes.Message;
import classes.utils.AppPreference;
import netUtils.response.BaseResponse;

public class GetMessagesResponse extends BaseResponse
{
	private List<Message> messageList;
	
	public GetMessagesResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
            String currentNickname = AppPreference.getAppPreference().getCurrentUser().getNickname();
			
			JSONArray jsonArray = getDataArray();
			messageList = new ArrayList<Message>();
			for (int i = 0 ; i < jsonArray.length() ; i++)
			{
				JSONObject jObject = jsonArray.getJSONObject(i);

                int type = jObject.getInt("type");
                if (type == Message.TYPE_MESSAGE)
                {
                    messageList.add(new Message(jObject));
                }
                else if (type == Message.TYPE_INVITE)
                {
                    messageList.add(new Invite(jObject, currentNickname));
                }
                else
                {
                    messageList.add(new Apply(jObject, currentNickname));
                }
			}			
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}		
	}

	public List<Message> getMessageList()
	{
		return messageList;
	}
}

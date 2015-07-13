package netUtils.response.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Apply;
import classes.model.Invite;
import classes.model.Message;
import classes.utils.AppPreference;
import netUtils.response.common.BaseResponse;

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
            int currentUserID = AppPreference.getAppPreference().getCurrentUserID();

            JSONArray jsonArray = getDataArray();
            messageList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++)
            {
                JSONObject jObject = jsonArray.getJSONObject(i);

                int type = jObject.getInteger("type");
                if (type == Message.TYPE_MESSAGE)
                {
                    messageList.add(new Message(jObject));
                }
                else if (type == Message.TYPE_INVITE)
                {
                    messageList.add(new Invite(jObject, currentUserID));
                }
                else
                {
                    messageList.add(new Apply(jObject, currentUserID));
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

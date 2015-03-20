package classes;

import com.rushucloud.reim.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import classes.utils.ReimApplication;
import classes.utils.Utils;

public class Message implements Serializable
{
	private static final long serialVersionUID = 1L;

    public static final int TYPE_MESSAGE = 1;
    public static final int TYPE_INVITE = 2;

    private int serverID = -1;
    private String title = "";
	private String content = "";
	private int updateTime = -1;
	private int type = -1;
    private boolean hasBeenRead = false;

    public Message()
    {

    }

    public Message(JSONObject jObject)
    {
        try
        {
            setServerID(jObject.getInt("id"));
            setTitle(ReimApplication.getContext().getString(R.string.message_from_admin));
            setUpdateTime(jObject.getInt("feedts"));
            setType(TYPE_MESSAGE);
            setHasBeenRead(Utils.intToBoolean(jObject.getInt("sread")));

            int time = jObject.getInt("createdts");
            String content = jObject.getString("feedback") + "\n\n-----以下是你于" +
                    Utils.secondToStringUpToDay(time) + "发送的内容-----\n\n" + jObject.getString("content");
            setContent(content);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getServerID()
    {
        return serverID;
    }
    public void setServerID(int serverID)
    {
        this.serverID = serverID;
    }

    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }
    public void setContent(String content)
    {
        this.content = content;
    }

    public int getUpdateTime()
    {
        return updateTime;
    }
    public void setUpdateTime(int updateTime)
    {
        this.updateTime = updateTime;
    }

    public int getType()
    {
        return type;
    }
    public void setType(int type)
    {
        this.type = type;
    }

    public boolean hasBeenRead()
    {
        return hasBeenRead;
    }
    public void setHasBeenRead(boolean hasBeenRead)
    {
        this.hasBeenRead = hasBeenRead;
    }

    public static void sortByUpdateDate(List<Message> messageList)
    {
    	Collections.sort(messageList, new Comparator<Message>()
		{
			public int compare(Message message1, Message message2)
			{
				return (int) (message2.getUpdateTime() - message1.getUpdateTime());
			}
		});
    }
}
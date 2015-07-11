package classes.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import classes.utils.DBManager;

public class ModifyHistory implements Serializable
{
    private static final long serialVersionUID = 1L;

    private User user;
    private String time = "";
    private String content = "";

    public ModifyHistory(JSONObject jObject)
    {
        try
        {
            setUser(DBManager.getDBManager().getUser(jObject.getInt("manager")));
            setTime(jObject.getString("submitdt"));
            setContent(jObject.getString("newvalue"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public User getUser()
    {
        return user;
    }
    public void setUser(User user)
    {
        this.user = user;
    }

    public String getTime()
    {
        return time;
    }
    public void setTime(String time)
    {
        this.time = time;
    }

    public String getContent()
    {
        return content;
    }
    public void setContent(String content)
    {
        this.content = content;
    }
}
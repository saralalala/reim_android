package classes.model;

import org.json.JSONException;
import org.json.JSONObject;

public class SetOfBook
{
    private int serverID = -1;
    private String name = "";
    private int userID = -1;

    public SetOfBook()
    {

    }

    public SetOfBook(JSONObject jObject, int userID)
    {
        try
        {
            setServerID(jObject.getInt("id"));
            setName(jObject.getString("name"));
            setUserID(userID);
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

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public int getUserID()
    {
        return userID;
    }
    public void setUserID(int userID)
    {
        this.userID = userID;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        SetOfBook group = (SetOfBook) o;
        return serverID == group.serverID;
    }
}

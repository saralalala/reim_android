package classes.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Group
{
    private int serverID = -1;
    private String name = "";
    private int createdDate = -1;
    private int serverUpdatedDate = -1;
    private int localUpdatedDate = -1;

    public Group()
    {

    }

    public Group(JSONObject jObject)
    {
        try
        {
            setServerID(jObject.getInt("groupid"));
            setName(jObject.getString("group_name"));
            setLocalUpdatedDate(jObject.getInt("lastdt"));
            setServerUpdatedDate(jObject.getInt("lastdt"));
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

    public int getCreatedDate()
    {
        return createdDate;
    }
    public void setCreatedDate(int createdDate)
    {
        this.createdDate = createdDate;
    }

    public int getServerUpdatedDate()
    {
        return serverUpdatedDate;
    }
    public void setServerUpdatedDate(int serverUpdatedDate)
    {
        this.serverUpdatedDate = serverUpdatedDate;
    }

    public int getLocalUpdatedDate()
    {
        return localUpdatedDate;
    }
    public void setLocalUpdatedDate(int localUpdatedDate)
    {
        this.localUpdatedDate = localUpdatedDate;
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

        Group group = (Group) o;
        return serverID == group.serverID;
    }
}

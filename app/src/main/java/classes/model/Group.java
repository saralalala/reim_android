package classes.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class Group
{
    private int serverID = -1;
    private String name = "";
    private boolean reportCanBeClosedDirectly = false;
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
            setServerID(jObject.getInteger("groupid"));
            setName(jObject.getString("group_name"));
            setLocalUpdatedDate(jObject.getInteger("lastdt"));
            setServerUpdatedDate(jObject.getInteger("lastdt"));

            String config = jObject.getString("config");
            if (!config.isEmpty())
            {
                JSONObject object = JSON.parseObject(config);
                if (object.containsKey("close_directly") && object.getInteger("close_directly") == 1)
                {
                    setReportCanBeClosedDirectly(true);
                }
            }
        }
        catch (com.alibaba.fastjson.JSONException e)
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

    public boolean reportCanBeClosedDirectly()
    {
        return reportCanBeClosedDirectly;
    }
    public void setReportCanBeClosedDirectly(boolean reportCanBeClosedDirectly)
    {
        this.reportCanBeClosedDirectly = reportCanBeClosedDirectly;
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

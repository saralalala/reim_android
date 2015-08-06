package classes.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class Group
{
    private int serverID = -1;
    private String name = "";
    private boolean reportCanBeClosedDirectly = false;
    private boolean showStructure = true;
    private boolean noAutoTime = false;
    private boolean isTimeCompulsory = false;
    private boolean isNoteCompulsory = false;
    private boolean isBudgetDisabled = false;
    private boolean isBorrowDisabled = false;
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

                if (object.containsKey("private_structure") && object.getInteger("private_structure") == 1)
                {
                    setShowStructure(false);
                }

                if (object.containsKey("not_auto_time") && object.getInteger("not_auto_time") == 1)
                {
                    setNoAutoTime(true);
                }

                if (object.containsKey("not_auto_time") && object.getInteger("not_auto_time") == 1)
                {
                    setIsTimeCompulsory(true);
                }

                if (object.containsKey("note_compulsory") && object.getInteger("note_compulsory") == 1)
                {
                    setIsNoteCompulsory(true);
                }

                if (object.containsKey("disable_budget") && object.getInteger("disable_budget") == 1)
                {
                    setIsBudgetDisabled(true);
                }

                if (object.containsKey("disable_borrow") && object.getInteger("disable_borrow") == 1)
                {
                    setIsBorrowDisabled(true);
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

    public boolean showStructure()
    {
        return showStructure;
    }
    public void setShowStructure(boolean showStructure)
    {
        this.showStructure = showStructure;
    }

    public boolean noAutoTime()
    {
        return noAutoTime;
    }
    public void setNoAutoTime(boolean noAutoTime)
    {
        this.noAutoTime = noAutoTime;
    }

    public boolean isTimeCompulsory()
    {
        return isTimeCompulsory;
    }
    public void setIsTimeCompulsory(boolean isTimeCompulsory)
    {
        this.isTimeCompulsory = isTimeCompulsory;
    }

    public boolean isNoteCompulsory()
    {
        return isNoteCompulsory;
    }
    public void setIsNoteCompulsory(boolean isNoteCompulsory)
    {
        this.isNoteCompulsory = isNoteCompulsory;
    }

    public boolean isBudgetDisabled()
    {
        boolean b = isBudgetDisabled;
        return isBudgetDisabled;
    }
    public void setIsBudgetDisabled(boolean isBudgetDisabled)
    {
        this.isBudgetDisabled = isBudgetDisabled;
    }

    public boolean isBorrowDisabled()
    {
        return isBorrowDisabled;
    }
    public void setIsBorrowDisabled(boolean isBorrowDisabled)
    {
        this.isBorrowDisabled = isBorrowDisabled;
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

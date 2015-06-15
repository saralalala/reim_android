package classes.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class BankAccount implements Serializable
{
    private int localID = -1;
    private int serverID = -1;
    private String name = "";
    private String number = "";
    private String bankName = "";
    private String location = "";

    public BankAccount()
    {

    }

    public BankAccount(JSONObject jObject)
    {
        try
        {
            setServerID(jObject.getInt("id"));
            setName(jObject.getString("account"));
            setNumber(jObject.getString("cardno"));
            setBankName(jObject.getString("bankname"));
            setLocation(jObject.getString("bankloc"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getLocalID()
    {
        return localID;
    }
    public void setLocalID(int localID)
    {
        this.localID = localID;
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

    public String getNumber()
    {
        return number;
    }
    public void setNumber(String number)
    {
        this.number = number;
    }

    public String getBankName()
    {
        return bankName;
    }
    public void setBankName(String bankName)
    {
        this.bankName = bankName;
    }

    public String getLocation()
    {
        return location;
    }
    public void setLocation(String location)
    {
        this.location = location;
    }

    public boolean equals(Object o)
    {
        if (o instanceof BankAccount)
        {
            BankAccount account = (BankAccount) o;
            return account.getServerID() == this.getServerID();
        }
        return super.equals(o);
    }
}
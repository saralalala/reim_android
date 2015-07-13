package classes.model;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

import classes.utils.Utils;

public class StatDepartment
{
    private int departmentID = -1;
    private String name = "";
    private double amount = 0;
    private boolean isDepartment = true;

    public StatDepartment(JSONObject jObject, boolean isDepartment)
    {
        try
        {
            setDepartmentID(Utils.optInt(jObject, "id", -1));
            setName(jObject.getString("name"));
            setAmount(jObject.getDouble("amount"));
            setIsDepartment(isDepartment);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getDepartmentID()
    {
        return departmentID;
    }
    public void setDepartmentID(int departmentID)
    {
        this.departmentID = departmentID;
    }

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public double getAmount()
    {
        return amount;
    }
    public void setAmount(double amount)
    {
        this.amount = amount;
    }

    public boolean isDepartment()
    {
        return isDepartment;
    }
    public void setIsDepartment(boolean isDepartment)
    {
        this.isDepartment = isDepartment;
    }

    public static boolean containsDepartment(List<StatDepartment> departmentList)
    {
        for (StatDepartment department : departmentList)
        {
            if (department.isDepartment())
            {
                return true;
            }
        }
        return false;
    }
}
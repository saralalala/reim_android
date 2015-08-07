package classes.model;

import android.util.SparseArray;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.utils.Utils;

public class ItemAttribution
{
    public static final int TYPE_EDITTEXT = 0;
    public static final int TYPE_SINGLE_CHOICE = 0;
    public static final int TYPE_TIME = 0;
    public static final int TYPE_TOGGLE = 0;

    private int id = -1;
    private String name = "";
    private int type = -1;
    private SparseArray<String> options = new SparseArray<>();
    private boolean isCompulsory = false;
    private boolean isDisabled = false;
    private ArrayList<Integer> relevantCategoryIDs = new ArrayList<>();

    public ItemAttribution()
    {

    }

    public ItemAttribution(JSONObject jObject)
    {
        try
        {
            setID(jObject.getInteger("id"));
            setName(jObject.getString("name"));
            setType(jObject.getInteger("type"));
            setIsCompulsory(Utils.intToBoolean(jObject.getInteger("force_input")));
            setIsDisabled(Utils.intToBoolean(jObject.getInteger("disabled")));
            relevantCategoryIDs.addAll(Utils.stringToIntList(jObject.getString("cid")));

            if (type == TYPE_SINGLE_CHOICE)
            {
                JSONArray optionArray = JSON.parseArray(jObject.getString("options"));
                for (int i = 0; i < optionArray.size(); i++)
                {
                    JSONObject object = optionArray.getJSONObject(i);
                    options.append(object.getInteger("id"), object.getString("name"));
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getID()
    {
        return id;
    }
    public void setID(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public int getType()
    {
        return type;
    }
    public void setType(int type)
    {
        this.type = type;
    }

    public SparseArray<String> getOptions()
    {
        return options;
    }
    public void setOptions(SparseArray<String> options)
    {
        this.options = options;
    }

    public boolean isCompulsory()
    {
        return isCompulsory;
    }
    public void setIsCompulsory(boolean isCompulsory)
    {
        this.isCompulsory = isCompulsory;
    }

    public boolean isDisabled()
    {
        return isDisabled;
    }
    public void setIsDisabled(boolean isDisabled)
    {
        this.isDisabled = isDisabled;
    }

    public ArrayList<Integer> getRelevantCategoryIDs()
    {
        return relevantCategoryIDs;
    }
    public void setRelevantCategoryIDs(ArrayList<Integer> relevantCategoryIDs)
    {
        this.relevantCategoryIDs = relevantCategoryIDs;
    }

    public static List<ItemAttribution> parseString(String text)
    {
        List<ItemAttribution> attributions = new ArrayList<>();
        JSONArray attributionArray = JSON.parseArray(text);
        for (int i = 0; i < attributionArray.size(); i++)
        {
            attributions.add(new ItemAttribution(attributionArray.getJSONObject(i)));
        }
        return attributions;
    }
}
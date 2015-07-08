package classes.model;

import com.rushucloud.reim.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.utils.DBManager;

public class Proxy implements Serializable
{
    public static final int PERMISSION_ALL = 0;
    public static final int PERMISSION_APPROVE = 1;
    public static final int PERMISSION_SUBMIT = 2;

    private User user;
    private int permission = PERMISSION_ALL;

    public User getUser()
    {
        return user;
    }
    public void setUser(User user)
    {
        this.user = user;
    }

    public int getPermission()
    {
        return permission;
    }
    public int getPermissionString()
    {
        switch (permission)
        {
            case PERMISSION_ALL:
                return R.string.proxy_permission_all;
            case PERMISSION_APPROVE:
                return R.string.proxy_permission_approve;
            case PERMISSION_SUBMIT:
                return R.string.proxy_permission_submit;
            default:
                return R.string.not_available;
        }
    }
    public void setPermission(int permission)
    {
        this.permission = permission;
    }

    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }

        if (o instanceof Proxy)
        {
            Proxy proxy = (Proxy) o;
            return proxy.getUser().getServerID() == this.user.getServerID();
        }
        return super.equals(o);
    }

    public static List<Proxy> parse(JSONArray jsonArray, boolean isClientArray)
    {
        List<Proxy> proxyList = new ArrayList<>();
        try
        {
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject object = jsonArray.getJSONObject(i);
                int userID = isClientArray ? object.getInt("uid") : object.getInt("wingid");
                User user = DBManager.getDBManager().getUser(userID);
                if (user == null)
                {
                    continue;
                }
                Proxy proxy = new Proxy();
                proxy.setUser(user);
                proxy.setPermission(object.getInt("permission"));
                proxyList.add(proxy);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return proxyList;
    }
}
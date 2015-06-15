package classes.model;

import com.rushucloud.reim.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import classes.utils.Utils;
import classes.utils.ViewUtils;

public class Invite extends Message implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_NEW = 0;
    public static final int TYPE_REJECTED = 1;
    public static final int TYPE_ACCEPTED = 2;

    private String inviteCode = "";
    private String invitor = "";
    private int typeCode = -1;

    public Invite()
    {

    }

    public Invite(JSONObject jObject, int currentUserID)
    {
        try
        {
            int invitorID = jObject.getInt("uid");
            String invitor = jObject.getString("invitor");
            String groupName = jObject.getString("groupname");
            String iName = jObject.optString("iname", "");
            int activeType = jObject.getInt("actived");

            setServerID(jObject.getInt("id"));
            setInviteCode(jObject.getString("code"));
            setTypeCode(activeType);
            setInvitor(invitor);
            setType(Message.TYPE_INVITE);
            setHasBeenRead(Utils.intToBoolean(jObject.getInt("sread")));

            if (invitorID != currentUserID && activeType == Invite.TYPE_NEW)
            {
                String message = String.format(ViewUtils.getString(R.string.invite_others_new), invitor, groupName);
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("invitedt"));
            }
            else if (invitorID == currentUserID && activeType == Invite.TYPE_NEW)
            {
                String message = String.format(ViewUtils.getString(R.string.invite_new), iName, groupName);
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("invitedt"));
            }
            else if (invitorID != currentUserID && activeType == Invite.TYPE_REJECTED)
            {
                String message = String.format(ViewUtils.getString(R.string.invite_others_rejected), groupName);
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("activedt"));
            }
            else if (invitorID == currentUserID && activeType == Invite.TYPE_REJECTED)
            {
                String message = String.format(ViewUtils.getString(R.string.invite_rejected), iName, groupName);
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("activedt"));
            }
            else if (invitorID != currentUserID && activeType == Invite.TYPE_ACCEPTED)
            {
                String message = String.format(ViewUtils.getString(R.string.invite_others_accepted), groupName);
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("activedt"));
            }
            else
            {
                String message = String.format(ViewUtils.getString(R.string.invite_accepted), iName, groupName);
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("activedt"));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public String getInviteCode()
    {
        return inviteCode;
    }
    public void setInviteCode(String inviteCode)
    {
        this.inviteCode = inviteCode;
    }

    public String getInvitor()
    {
        return invitor;
    }
    public void setInvitor(String invitor)
    {
        this.invitor = invitor;
    }

    public int getTypeCode()
    {
        return typeCode;
    }
    public void setTypeCode(int typeCode)
    {
        this.typeCode = typeCode;
    }
}
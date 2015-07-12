package classes.model;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.rushucloud.reim.R;

import java.io.Serializable;

import classes.utils.Utils;
import classes.utils.ViewUtils;

public class Apply extends Message implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_NEW = 0;
    public static final int TYPE_REJECTED = 1;
    public static final int TYPE_ACCEPTED = 2;

    private String applicant = "";
    private int typeCode = -1;

    public Apply()
    {

    }

    public Apply(JSONObject jObject, int currentUserID)
    {
        try
        {
            int applicantID = jObject.getInteger("uid");
            String applicant = jObject.getString("applicant");
            String groupName = jObject.getString("groupname");
            int activeType = jObject.getInteger("permit");

            setServerID(jObject.getInteger("id"));
            setTypeCode(activeType);
            setApplicant(applicant);
            setType(Message.TYPE_APPLY);
            setHasBeenRead(Utils.intToBoolean(jObject.getInteger("sread")));

            if (applicantID != currentUserID && activeType == Apply.TYPE_NEW)
            {
                String message = String.format(ViewUtils.getString(R.string.apply_others_new), applicant, groupName);
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInteger("updatedt"));
            }
            else if (applicantID == currentUserID && activeType == Apply.TYPE_NEW)
            {
                String message = String.format(ViewUtils.getString(R.string.apply_new), groupName);
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInteger("updatedt"));
            }
            else if (applicantID != currentUserID && activeType == Apply.TYPE_REJECTED)
            {
                String message = String.format(ViewUtils.getString(R.string.apply_others_rejected), applicant, groupName);
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInteger("updatedt"));
            }
            else if (applicantID == currentUserID && activeType == Apply.TYPE_REJECTED)
            {
                String message = String.format(ViewUtils.getString(R.string.apply_rejected), groupName);
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInteger("updatedt"));
            }
            else if (applicantID != currentUserID && activeType == Apply.TYPE_ACCEPTED)
            {
                String message = String.format(ViewUtils.getString(R.string.apply_others_accepted), applicant, groupName);
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInteger("updatedt"));
            }
            else
            {
                String message = String.format(ViewUtils.getString(R.string.apply_accepted), groupName);
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInteger("updatedt"));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public String getApplicant()
    {
        return applicant;
    }
    public void setApplicant(String applicant)
    {
        this.applicant = applicant;
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
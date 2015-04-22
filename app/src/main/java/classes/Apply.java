package classes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import classes.utils.Utils;

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

    public Apply(JSONObject jObject, String currentNickname)
    {
        try
        {
            String applicant = jObject.getString("applicant");
            int activeType = jObject.getInt("permit");

            setServerID(jObject.getInt("id"));
            setTypeCode(activeType);
            setApplicant(applicant);
            setType(Message.TYPE_APPLY);
            setHasBeenRead(Utils.intToBoolean(jObject.getInt("sread")));

            if (!applicant.equals(currentNickname) && activeType == Apply.TYPE_NEW)
            {
                String message = applicant + " 申请加入「" + jObject.getString("groupname") + "」";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("updatedt"));
            }
            else if (applicant.equals(currentNickname) && activeType == Apply.TYPE_NEW)
            {
                String message = "你发送了加入「" + jObject.getString("groupname") + "」的申请";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("updatedt"));
            }
            else if (!applicant.equals(currentNickname) && activeType == Apply.TYPE_REJECTED)
            {
                String message = "你拒绝了 " + applicant + " 加入「" + jObject.getString("groupname") + "」的申请";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("updatedt"));
            }
            else if (applicant.equals(currentNickname) && activeType == Apply.TYPE_REJECTED)
            {
                String message = "你加入「" + jObject.getString("groupname") + "」的申请被拒绝";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("updatedt"));
            }
            else if (!applicant.equals(currentNickname) && activeType == Apply.TYPE_ACCEPTED)
            {
                String message = "你同意了 " + applicant + " 加入「" + jObject.getString("groupname") + "」的申请";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("updatedt"));
            }
            else
            {
                String message = "你加入「" + jObject.getString("groupname") + "」的申请已通过";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("updatedt"));
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
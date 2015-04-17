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

	private String applyCode = "";
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
            int activeType = jObject.getInt("actived");

            setServerID(jObject.getInt("id"));
            setApplyCode(jObject.getString("code"));
            setTypeCode(activeType);
            setApplicant(applicant);
            setType(Message.TYPE_APPLY);
            setHasBeenRead(Utils.intToBoolean(jObject.getInt("sread")));

            if (!applicant.equals(currentNickname) && activeType == Apply.TYPE_NEW)
            {
                String message = "用户" + applicant + "申请加入「" + jObject.getString("groupname") + "」";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("invitedt"));
            }
            else if (applicant.equals(currentNickname) && activeType == Apply.TYPE_NEW)
            {
                String message = "您发送了加入「" + jObject.getString("groupname") + "」的申请";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("invitedt"));
            }
            else if (!applicant.equals(currentNickname) && activeType == Apply.TYPE_REJECTED)
            {
                String message = "您拒绝了" + applicant + "加入「" + jObject.getString("groupname") + "」的申请";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("activedt"));
            }
            else if (applicant.equals(currentNickname) && activeType == Apply.TYPE_REJECTED)
            {
                String message = "您加入「" + jObject.getString("groupname") + "」的申请被拒绝";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("activedt"));
            }
            else if (!applicant.equals(currentNickname) && activeType == Apply.TYPE_ACCEPTED)
            {
                String message = "您批准了" + applicant + "加入「" + jObject.getString("groupname") + "」的申请";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("activedt"));
            }
            else
            {
                String message = "您加入「" + jObject.getString("groupname") + "」的申请已通过";
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

    public String getApplyCode()
    {
        return applyCode;
    }
    public void setApplyCode(String applyCode)
    {
        this.applyCode = applyCode;
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
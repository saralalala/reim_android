package classes.model;

import android.util.SparseArray;

import com.rushucloud.reim.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.Utils;

public class Report implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_REIM = 0;
    public static final int TYPE_BUDGET = 1;
    public static final int TYPE_BORROWING = 2;

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_SUBMITTED = 1;
    public static final int STATUS_APPROVED = 2;
    public static final int STATUS_REJECTED = 3;
    public static final int STATUS_FINISHED = 4;
    public static final int STATUS_NEED_CONFIRM = 7;
    public static final int STATUS_CONFIRMED = 8;

    private int localID = -1;
    private int serverID = -1;
    private int type = TYPE_REIM;
    private String title = "";
    private int status = Report.STATUS_DRAFT;
    private int myDecision = Report.STATUS_SUBMITTED;
    private boolean aaApproved = false;
    private List<User> managerList = null;
    private List<User> ccList = null;
    private List<Comment> commentList = null;
    private User sender = null;
    private int createdDate = -1;
    private int serverUpdatedDate = -1;
    private int localUpdatedDate = -1;
    private int itemCount;
    private String amount;
    private boolean isCC;
    private int step = 0;
    private String sectionName = "";

    public Report()
    {

    }

    public Report(Report report)
    {
        localID = report.getLocalID();
        serverID = report.getServerID();
        title = report.getTitle();
        status = report.getStatus();
        myDecision = report.getMyDecision();
        managerList = new ArrayList<>(report.getManagerList());
        ccList = new ArrayList<>(report.getCCList());
        commentList = new ArrayList<>(report.getCommentList());
        sender = new User(report.getSender());
        type = report.getType();
        createdDate = report.getCreatedDate();
        serverUpdatedDate = report.getServerUpdatedDate();
        localUpdatedDate = report.getLocalUpdatedDate();
        amount = report.getAmount();
        itemCount = report.getItemCount();
        isCC = report.isCC();
        step = report.getStep();
    }

    public Report(JSONObject jObject)
    {
        try
        {
            setServerID(jObject.getInt("id"));
            setTitle(jObject.getString("title"));
            setCreatedDate(jObject.getInt("createdt"));
            setStatus(jObject.getInt("status"));
            setType(jObject.getInt("prove_ahead"));
            setAaApproved(Utils.intToBoolean(jObject.getInt("pa_approval")));
            setLocalUpdatedDate(jObject.getInt("lastdt"));
            setServerUpdatedDate(jObject.getInt("lastdt"));

            User user = new User();
            user.setServerID(jObject.getInt("uid"));
            setSender(user);
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

    public int getType()
    {
        return type;
    }
    public void setType(int type)
    {
        this.type = type;
    }

    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }

    public int getStatus()
    {
        return status;
    }
    public void setStatus(int status)
    {
        this.status = status;
    }

    public int getMyDecision()
    {
        return myDecision;
    }
    public void setMyDecision(int myDecision)
    {
        this.myDecision = myDecision;
    }

    public boolean isAaApproved()
    {
        return aaApproved;
    }
    public void setAaApproved(boolean aaApproved)
    {
        this.aaApproved = aaApproved;
    }

    public List<User> getManagerList()
    {
        return managerList;
    }
    public String getManagersName()
    {
        if (getManagerList() == null || getManagerList().isEmpty())
        {
            return "";
        }
        else
        {
            return User.getUsersNameString(getManagerList());
        }
    }
    public void setManagerList(List<User> managerList)
    {
        if (managerList == null)
        {
            this.managerList = new ArrayList<>();
        }
        else if (this.managerList != null)
        {
            this.managerList.clear();
            this.managerList.addAll(managerList);
        }
        else
        {
            this.managerList = managerList;
        }
    }

    public List<User> getCCList()
    {
        return ccList;
    }
    public String getCCsName()
    {
        if (getCCList() == null || getCCList().isEmpty())
        {
            return "";
        }
        else
        {
            return User.getUsersNameString(getCCList());
        }
    }
    public void setCCList(List<User> ccList)
    {
        if (ccList == null)
        {
            this.ccList = new ArrayList<>();
        }
        else if (this.ccList != null)
        {
            this.ccList.clear();
            this.ccList.addAll(ccList);
        }
        else
        {
            this.ccList = ccList;
        }
    }

    public List<Comment> getCommentList()
    {
        return commentList;
    }
    public void setCommentList(List<Comment> commentList)
    {
        this.commentList = commentList;
    }

    public User getSender()
    {
        return sender;
    }
    public void setSender(User sender)
    {
        this.sender = sender;
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

    public int getItemCount()
    {
        return itemCount;
    }
    public void setItemCount(int count)
    {
        this.itemCount = count;
    }

    public String getAmount()
    {
        return amount;
    }
    public void setAmount(String amount)
    {
        this.amount = amount;
    }

    public boolean isCC()
    {
        return isCC;
    }
    public void setIsCC(boolean isCC)
    {
        this.isCC = isCC;
    }

    public int getStep()
    {
        return step;
    }
    public void setStep(int step)
    {
        this.step = step;
    }

    public String getSectionName()
    {
        return sectionName;
    }
    public void setSectionName(String sectionName)
    {
        this.sectionName = sectionName;
    }

    public int getStatusBackground()
    {
        switch (getStatus())
        {
            case STATUS_DRAFT:
                return R.drawable.status_draft;
            case STATUS_SUBMITTED:
                return R.drawable.status_submitted;
            case STATUS_APPROVED:
                return R.drawable.status_approved;
            case STATUS_REJECTED:
                return R.drawable.status_rejected;
            case STATUS_FINISHED:
                return R.drawable.status_finished;
            case STATUS_NEED_CONFIRM:
                return R.drawable.status_finished;
            case STATUS_CONFIRMED:
                return R.drawable.status_finished;
            default:
                return 0;
        }
    }

    public int getStatusString()
    {
        switch (getStatus())
        {
            case STATUS_DRAFT:
                return R.string.status_draft;
            case STATUS_SUBMITTED:
                return R.string.status_submitted;
            case STATUS_APPROVED:
                return R.string.status_approved;
            case STATUS_REJECTED:
                return R.string.status_rejected;
            case STATUS_FINISHED:
                return R.string.status_finished;
            case STATUS_NEED_CONFIRM:
                return R.string.status_finished;
            case STATUS_CONFIRMED:
                return R.string.status_finished;
            default:
                return R.string.not_available;
        }
    }

    public boolean canBeSubmitted()
    {
        double amount = 0;
        List<Item> itemList = DBManager.getDBManager().getReportItems(localID);
        for (Item item : itemList)
        {
            if (!item.canBeSubmitWithReport())
            {
                return false;
            }
            amount += item.getAmount();
        }

        return amount != 0;
    }

    public boolean isEditable()
    {
        return getStatus() == Report.STATUS_DRAFT || getStatus() == Report.STATUS_REJECTED;
    }

    public boolean isFinished()
    {
        return getStatus() == Report.STATUS_FINISHED || getStatus() == Report.STATUS_NEED_CONFIRM || getStatus() == STATUS_CONFIRMED;
    }

    public boolean canBeApproved()
    {
        return getSender() != null && !getSender().equals(AppPreference.getAppPreference().getCurrentUser()) && !isCC &&
                status == Report.STATUS_SUBMITTED && myDecision == Report.STATUS_SUBMITTED;
    }

    public boolean canBeApprovedByMe()
    {
        return !isCC && status == Report.STATUS_SUBMITTED && myDecision == Report.STATUS_SUBMITTED;
    }

    public boolean isPending()
    {
        return canBeApprovedByMe() || (isCC && status == Report.STATUS_SUBMITTED && (managerList == null || managerList.isEmpty())); // 刚取的报告，还没获取详细数据，因此没managerList
    }

    public boolean isInSpecificStatus(List<Integer> statusList)
    {
        for (Integer integer : statusList)
        {
            if (getStatus() == integer)
            {
                return true;
            }
            if (integer == Report.STATUS_FINISHED && isFinished())
            {
                return true;
            }
        }
        return false;
    }

    public static int getStatusString(int status)
    {
        switch (status)
        {
            case STATUS_DRAFT:
                return R.string.status_draft;
            case STATUS_SUBMITTED:
                return R.string.status_submitted;
            case STATUS_APPROVED:
                return R.string.status_approved;
            case STATUS_REJECTED:
                return R.string.status_rejected;
            case STATUS_FINISHED:
                return R.string.status_finished;
            case STATUS_NEED_CONFIRM:
                return R.string.status_finished;
            case STATUS_CONFIRMED:
                return R.string.status_finished;
            default:
                return R.string.not_available;
        }
    }

    public static void sortMineByAmount(List<Report> reportList)
    {
        DBManager dbManager = DBManager.getDBManager();
        final SparseArray<Double> countArray = new SparseArray<>();
        for (Report report : reportList)
        {
            double amount = dbManager.getReportAmount(report.getLocalID());
            countArray.put(report.getLocalID(), amount);
        }

        Collections.sort(reportList, new Comparator<Report>()
        {
            public int compare(Report report1, Report report2)
            {
                return (int) (countArray.get(report2.getLocalID()) - countArray.get(report1.getLocalID()));
            }
        });
    }

    public static void sortOthersByAmount(List<Report> reportList)
    {
        Collections.sort(reportList, new Comparator<Report>()
        {
            public int compare(Report report1, Report report2)
            {
                double amount1 = Double.valueOf(report1.getAmount());
                double amount2 = Double.valueOf(report2.getAmount());
                if (Math.abs(amount1 - amount2) < 0.01)
                {
                    return 0;
                }
                else
                {
                    return amount1 > amount2 ? -1 : 1;
                }
            }
        });
    }

    public static void sortByCreateDate(List<Report> reportList)
    {
        Collections.sort(reportList, new Comparator<Report>()
        {
            public int compare(Report report1, Report report2)
            {
                return report2.getCreatedDate() - report1.getCreatedDate();
            }
        });
    }

    public static void sortByUpdateDate(List<Report> reportList)
    {
        Collections.sort(reportList, new Comparator<Report>()
        {
            public int compare(Report report1, Report report2)
            {
                return report2.getLocalUpdatedDate() - report1.getLocalUpdatedDate();
            }
        });
    }
}
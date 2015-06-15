package classes.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Comment implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int localID = -1;
    private int serverID = -1;
    private int reportID = -1;
    private String content = "";
    private User reviewer = null;
    private int createdDate = -1;
    private int serverUpdatedDate = -1;
    private int localUpdatedDate = -1;

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

    public int getReportID()
    {
        return reportID;
    }
    public void setReportID(int reportID)
    {
        this.reportID = reportID;
    }

    public String getContent()
    {
        return content;
    }
    public void setContent(String content)
    {
        this.content = content;
    }

    public User getReviewer()
    {
        return reviewer;
    }
    public void setReviewer(User reviewer)
    {
        this.reviewer = reviewer;
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

    public static void sortByCreateDate(List<Comment> commentList)
    {
        Collections.sort(commentList, new Comparator<Comment>()
        {
            public int compare(Comment comment1, Comment comment2)
            {
                return comment2.getCreatedDate() - comment1.getCreatedDate();
            }
        });
    }
}
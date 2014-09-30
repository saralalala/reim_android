package classes;

import java.util.List;

import android.graphics.Bitmap;

public class Item
{
	private int id = -1;
	private Bitmap image = null;
	private int imageID = -1;
	private String merchant = "";
	private Report belongReport = null;
	private Package belongPackage = null;
	private Category category = null;
	private double amount = -1;
	private User consumer;
	private List<User> relevantUsers = null;
	private List<Tag> tag = null;
	private int serverUpdatedDate = -1;
	private int localUpdatedDate = -1;
	private int consumedDate = -1;
	private String note = "";
	private Boolean billable = false;
	private int status = -1;
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public Bitmap getImage()
	{
		return image;
	}
	public void setImage(Bitmap image)
	{
		this.image = image;
	}
	public int getImageID()
	{
		return imageID;
	}
	public void setImageID(int imageID)
	{
		this.imageID = imageID;
	}
	public String getMerchant()
	{
		return merchant;
	}
	public void setMerchant(String merchant)
	{
		this.merchant = merchant;
	}
	public Report getBelongReport()
	{
		return belongReport;
	}
	public void setBelongReport(Report belongReport)
	{
		this.belongReport = belongReport;
	}
	public Package getBelongPackage()
	{
		return belongPackage;
	}
	public void setBelongPackage(Package belongPackage)
	{
		this.belongPackage = belongPackage;
	}
	public Category getCategory()
	{
		return category;
	}
	public void setCategory(Category category)
	{
		this.category = category;
	}
	public double getAmount()
	{
		return amount;
	}
	public void setAmount(double amount)
	{
		this.amount = amount;
	}
	public User getConsumer()
	{
		return consumer;
	}
	public void setConsumer(User consumer)
	{
		this.consumer = consumer;
	}
	public List<User> getRelevantUsers()
	{
		return relevantUsers;
	}
	public void setRelevantUsers(List<User> relevantUsers)
	{
		this.relevantUsers = relevantUsers;
	}
	public List<Tag> getTag()
	{
		return tag;
	}
	public void setTag(List<Tag> tag)
	{
		this.tag = tag;
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
	public int getConsumedDate()
	{
		return consumedDate;
	}
	public void setConsumedDate(int consumedDate)
	{
		this.consumedDate = consumedDate;
	}
	public String getNote()
	{
		return note;
	}
	public void setNote(String note)
	{
		this.note = note;
	}
	public Boolean getBillable()
	{
		return billable;
	}
	public void setBillable(Boolean billable)
	{
		this.billable = billable;
	}
	public int getStatus()
	{
		return status;
	}
	public void setStatus(int status)
	{
		this.status = status;
	}
}

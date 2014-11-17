package classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class Item
{		
	public static final int STATUS_DRAFT = 0;
	public static final int STATUS_SUBMITTED = 1;
	public static final int STATUS_APPROVED = 2;
	public static final int STATUS_REJECTED = 3;
	public static final int STATUS_FINISHED = 4;
	public static final int STATUS_PROVE_AHEAD_APPROVED = 5;
	
	private int localID = -1;
	private int serverID = -1;
	private int imageID = -1;
	private String invoicePath = "";
	private String merchant = "";
	private Report belongReport = null;
	private Package belongPackage = null;
	private Category category = null;
	private double amount = 0.0;
	private User consumer;
	private int consumedDate = -1;
	private String note = "";
	private Boolean isProveAhead = false;
	private Boolean needReimbursed = false;
	private int status = STATUS_DRAFT;
	private String location = "";
	private List<User> relevantUsers = null;
	private List<Tag> tags = null;
	private String relevantUsersID = "";
	private String tagsID = "";
	private int createdDate = -1;
	private int serverUpdatedDate = -1;
	private int localUpdatedDate = -1;
	
	public Item()
	{
		
	}
	
	public Item(JSONObject jObject)
	{
		try
		{
			setServerID(jObject.getInt("id"));
			setAmount(jObject.getDouble("amount"));
			setMerchant(jObject.getString("merchants"));
			setNote(jObject.getString("note"));
			setStatus(jObject.getInt("status"));
			setLocation(jObject.getString("location"));
			setConsumedDate(jObject.getInt("dt"));
			setCreatedDate(jObject.getInt("createdt"));		
			setServerUpdatedDate(jObject.getInt("lastdt"));				
			setLocalUpdatedDate(jObject.getInt("lastdt"));		
			setInvoicePath("");
			setIsProveAhead(Utils.intToBoolean(jObject.getInt("prove_ahead")));
			setNeedReimbursed(Utils.intToBoolean(jObject.getInt("reimbursed")));
			
			List<Integer> idList = Utils.stringToIntList(jObject.getString("image_id"));
			int imageID = idList.size() > 0 ? idList.get(0) : -1;
			setImageID(imageID);
			
			Report report = new Report();
			report.setServerID(jObject.getInt("rid"));
			setBelongReport(report);				
			
			Category category = new Category();
			category.setServerID(jObject.getInt("category"));
			setCategory(category);
			
			List<Tag> tagList = Tag.stringToTagList(jObject.getString("tags"));
			setTags(tagList);
			
//			List<User> userList = User.idStringToUserList(jObject.getString("relates"));
//			setRelevantUsers(userList);
			
			User user = new User();
			user.setServerID(jObject.getInt("uid"));
			setConsumer(user);
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
	
	public int getImageID()
	{
		return imageID;
	}
	public void setImageID(int imageID)
	{
		this.imageID = imageID;
	}
	
	public String getInvoicePath()
	{
		return invoicePath;
	}
	public void setInvoicePath(String invoicePath)
	{
		this.invoicePath = invoicePath;
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
	
	public List<Tag> getTags()
	{
		return tags;
	}
	public void setTags(List<Tag> tag)
	{
		this.tags = tag;
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
	
	public Boolean isProveAhead()
	{
		return isProveAhead;
	}
	public void setIsProveAhead(Boolean isProveAhead)
	{
		this.isProveAhead = isProveAhead;
	}
	
	public Boolean needReimbursed()	
	{
		return needReimbursed;
	}
	public void setNeedReimbursed(Boolean needReimbursed)
	{
		this.needReimbursed = needReimbursed;
	}

	public int getStatus()
	{
		return status;
	}
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	public String getLocation()
	{
		return location;
	}
	public void setLocation(String location)
	{
		this.location = location;
	}
	
	public String getRelevantUsersID()
	{
		return relevantUsersID;
	}
	public void setRelevantUsersID(String relevantUsersID)
	{
		this.relevantUsersID = relevantUsersID;
	}
	
	public String getTagsID()
	{
		return tagsID;
	}
	public void setTagsID(String tagsID)
	{
		this.tagsID = tagsID;
	}
	
    public static ArrayList<Integer> getItemsIDArray(List<Item> itemList)
    {
    	ArrayList<Integer> idArrayList = new ArrayList<Integer>();
    	for (int i = 0; i < itemList.size(); i++)
		{
			idArrayList.add(itemList.get(i).getLocalID());
		}
    	return idArrayList;
    }
	
	public Boolean canBeSubmitWithReport()
	{
		if (imageID == -1 && !invoicePath.equals(""))
		{
			return false;
		}
		if (serverID == -1)
		{
			return false;
		}
		return true;
	}
	
	public Boolean containsSpecificTags(List<Tag> tagList)
	{
		if (tags == null)
		{
			return false;
		}
		
		for (Tag tag : tags)
		{
			for (Tag targetTag : tagList)
			{
				if (tag.getName().equals(targetTag.getName()))
				{
					return true;
				}
			}
		}
		return false;
	}
    
    public static void sortByConsumedDate(List<Item> itemList)
    {
    	Collections.sort(itemList, new Comparator<Item>()
		{
			public int compare(Item item1, Item item2)
			{
				return (int)(item1.getConsumedDate() - item2.getConsumedDate());
			}
		});
    }
    
    public static void sortByAmount(List<Item> itemList)
    {
    	Collections.sort(itemList, new Comparator<Item>()
		{
			public int compare(Item item1, Item item2)
			{
				return (int)(item1.getAmount() - item2.getAmount());
			}
		});
    }
    
    public static void sortByUpdateDate(List<Item> itemList)
    {
    	Collections.sort(itemList, new Comparator<Item>()
		{
			public int compare(Item item1, Item item2)
			{
				return (int)(item2.getLocalUpdatedDate() - item1.getLocalUpdatedDate());
			}
		});
    }
}
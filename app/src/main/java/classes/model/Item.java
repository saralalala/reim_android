package classes.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

import classes.utils.DBManager;
import classes.utils.JSONUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;

public class Item
{
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

    public static final int MAX_INVOICE_COUNT = 16;

    private int localID = -1;
    private int serverID = -1;
    private int type = TYPE_REIM;
    private Report belongReport = null;
    private User consumer;
    private double amount = 0;
    private double aaAmount = 0; // approve ahead amount
    private boolean needReimbursed = true;
    private boolean aaApproved = false;
    private int status = STATUS_DRAFT;
    private Category category = null;
    private String vendor = "";
    private String location = "";
    private double latitude = -1;
    private double longitude = -1;
    private Currency currency = null;
    private double rate = 0;
    private int didiID = -1;
    private List<Image> invoices = null;
    private List<User> relevantUsers = null;
    private List<Tag> tags = null;
    private String note = "";
    private String relevantUsersID = "";
    private String tagsID = "";
    private String extraString = "";
    private int consumedDate = -1;
    private int createdDate = -1;
    private int serverUpdatedDate = -1;
    private int localUpdatedDate = -1;
    private String consumedDateGroup = "";

    public Item()
    {
        currency = DBManager.getDBManager().getCurrency("CNY");
    }

    public Item(JSONObject jObject)
    {
        try
        {
            setServerID(jObject.getInteger("id"));
            setAmount(jObject.getDouble("amount"));
            setAaAmount(jObject.getDouble("pa_amount"));
            setVendor(jObject.getString("merchants"));
            setNote(jObject.getString("note"));
            setStatus(jObject.getInteger("status"));
            setLocation(jObject.getString("location"));
            setConsumedDate(jObject.getInteger("dt"));
            setCreatedDate(jObject.getInteger("createdt"));
            setServerUpdatedDate(jObject.getInteger("lastdt"));
            setLocalUpdatedDate(jObject.getInteger("lastdt"));
            setType(jObject.getInteger("prove_ahead"));
            setNeedReimbursed(Utils.intToBoolean(jObject.getInteger("reimbursed")));
            setAaApproved(Utils.intToBoolean(jObject.getInteger("pa_approval")));
            setTags(Tag.idStringToTagList(jObject.getString("tags")));
            setTagsID(jObject.getString("tags"));
            setRelevantUsers(User.idStringToUserList(jObject.getString("relates")));
            setRelevantUsersID(jObject.getString("relates"));
            setExtraString(JSONUtils.optString(jObject, "extra", "[]"));

            Currency currency = new Currency();
            String currencyCode = JSONUtils.optString(jObject, "currency", "").toUpperCase();
            if (!currencyCode.isEmpty())
            {
                currency.setCode(currencyCode);
            }
            setCurrency(currency);
            setRate(jObject.getDouble("rate"));

            JSONArray invoiceArray = jObject.getJSONArray("images");
            List<Image> invoiceList = new ArrayList<>();
            for (int i = 0; i < invoiceArray.size(); i++)
            {
                Image image = new Image(invoiceArray.getJSONObject(i));
                invoiceList.add(image);
            }
            setInvoices(invoiceList);

            Report report = new Report();
            report.setServerID(jObject.getInteger("rid"));
            setBelongReport(report);

            Category category = new Category();
            category.setServerID(jObject.getInteger("category"));
            setCategory(category);

            User user = new User();
            user.setServerID(jObject.getInteger("uid"));
            setConsumer(user);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public Item(Item item)
    {
        category = new Category(item.getCategory());
        note = item.getNote();
        vendor = item.getVendor();
        tagsID = item.getTagsID();
        relevantUsersID = item.getRelevantUsersID();
        amount = item.getAmount();
        location = item.getLocation();
        consumedDate = item.getConsumedDate();
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
    public int getTypeString()
    {
        switch (getType())
        {
            case Item.TYPE_REIM:
                return R.string.consumed;
            case Item.TYPE_BUDGET:
                return R.string.budget;
            case Item.TYPE_BORROWING:
                return R.string.borrowing;
            default:
                return R.string.not_available;
        }
    }
    public void setType(int type)
    {
        this.type = type;
    }

    public Report getBelongReport()
    {
        return belongReport;
    }
    public void setBelongReport(Report belongReport)
    {
        this.belongReport = belongReport;
    }

    public User getConsumer()
    {
        return consumer;
    }
    public void setConsumer(User consumer)
    {
        this.consumer = consumer;
    }

    public double getAmount()
    {
        return amount;
    }
    public void setAmount(double amount)
    {
        this.amount = amount;
    }

    public double getAaAmount()
    {
        return aaAmount;
    }
    public void setAaAmount(double aaAmount)
    {
        this.aaAmount = aaAmount;
    }

    public boolean needReimbursed()
    {
        return needReimbursed;
    }
    public void setNeedReimbursed(boolean needReimbursed)
    {
        this.needReimbursed = needReimbursed;
    }

    public boolean isAaApproved()
    {
        return aaApproved;
    }
    public void setAaApproved(boolean aaApproved)
    {
        this.aaApproved = aaApproved;
    }

    public int getStatus()
    {
        return status;
    }
    public void setStatus(int status)
    {
        this.status = status;
    }

    public Category getCategory()
    {
        return category;
    }
    public void setCategory(Category category)
    {
        this.category = category;
    }

    public String getVendor()
    {
        return vendor;
    }
    public void setVendor(String vendor)
    {
        this.vendor = vendor;
    }

    public String getLocation()
    {
        return location.equals("0") ? "" : location;
    }
    public void setLocation(String location)
    {
        this.location = location;
    }

    public double getLatitude()
    {
        return latitude;
    }
    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }
    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public Currency getCurrency()
    {
        return currency;
    }
    public void setCurrency(Currency currency)
    {
        this.currency = currency;
    }

    public double getRate()
    {
        return rate;
    }
    public void setRate(double rate)
    {
        this.rate = rate;
    }

    public int getDidiID()
    {
        return didiID;
    }
    public void setDidiID(int didiID)
    {
        this.didiID = didiID;
    }

    public List<Image> getInvoices()
    {
        return invoices;
    }
    public void setInvoices(List<Image> invoices)
    {
        this.invoices = invoices;
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
    public void setTags(List<Tag> tags)
    {
        this.tags = tags;
    }

    public String getNote()
    {
        return note;
    }
    public void setNote(String note)
    {
        this.note = note;
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

    public String getExtraString()
    {
        return extraString;
    }
    public void setExtraString(String extraString)
    {
        this.extraString = extraString;
    }
    public int getDurationDays()
    {
        if (!getExtraString().isEmpty())
        {
            JSONArray extraArray = JSON.parseArray(getExtraString());
            if (extraArray != null)
            {
                int endTime = 0;
                for (int i = 0; i < extraArray.size(); i++)
                {
                    ItemAttribution attribution = new ItemAttribution();
                    int value = attribution.parse(extraArray.getJSONObject(i));
                    if (attribution.getType() == ItemAttribution.TYPE_TIME)
                    {
                        endTime = value;
                        break;
                    }
                }
                if (endTime != 0)
                {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis((long) getConsumedDate() * 1000);
                    GregorianCalendar greCal = new GregorianCalendar(calendar.get(Calendar.YEAR),
                                                                     calendar.get(Calendar.MONTH),
                                                                     calendar.get(Calendar.DAY_OF_MONTH), 0, 0);
                    int startStandardTime = (int) greCal.getTimeInMillis() / 1000;
                    boolean startAfternoon = calendar.get(Calendar.HOUR_OF_DAY) >= 12;

                    calendar.setTimeInMillis((long) endTime * 1000);
                    greCal.set(calendar.get(Calendar.YEAR),
                               calendar.get(Calendar.MONTH),
                               calendar.get(Calendar.DAY_OF_MONTH), 0, 0);
                    int endStandardTime = (int) greCal.getTimeInMillis() / 1000;
                    boolean endAfternoon = calendar.get(Calendar.HOUR_OF_DAY) >= 12;

                    int days = (endStandardTime - startStandardTime) / 86400;
                    days = days == 0 ? 1 : days;
                    days += startAfternoon ? 0 : 1;
                    days += endAfternoon ? 1 : 0;
                    return days;
                }
            }
        }
        return 0;
    }
    public String getDailyAverage(int days)
    {
        return days > 0 ? getCurrency().getSymbol() + Utils.formatAmount(getAmount() / days) +
                "/" + ViewUtils.getString(R.string.day) + "*" + days : "";
    }
    public int getMemberCount()
    {
        if (!getExtraString().isEmpty())
        {
            JSONArray extraArray = JSON.parseArray(getExtraString());
            if (extraArray != null)
            {
                for (int i = 0; i < extraArray.size(); i++)
                {
                    ItemAttribution attribution = new ItemAttribution();
                    int value = attribution.parse(extraArray.getJSONObject(i));
                    if (attribution.getType() == ItemAttribution.TYPE_MEMBER_COUNT)
                    {
                        return value;
                    }
                }
            }
        }
        return 1;
    }
    public String getPerCapita(int count)
    {
        String person = ViewUtils.getString(R.string.person);
        return getCurrency().getSymbol() + Utils.formatAmount(getAmount() / count) + "/" +
                 person + "*" + count + person;
    }

    public int getConsumedDate()
    {
        return consumedDate;
    }
    public void setConsumedDate(int consumedDate)
    {
        this.consumedDate = consumedDate;
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

    public String getConsumedDateGroup()
    {
        return consumedDateGroup;
    }
    public void setConsumedDateGroup(String consumedDateGroup)
    {
        this.consumedDateGroup = consumedDateGroup;
    }

    public int getStatusBackground()
    {
        if (getBelongReport() != null)
        {
            switch (getBelongReport().getStatus())
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
        else
        {
            return R.drawable.status_draft;
        }
    }

    public int getStatusString()
    {
        if (getBelongReport() != null)
        {
            switch (getBelongReport().getStatus())
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
        else
        {
            return R.string.status_draft;
        }
    }

    public boolean missingInfo(Group group)
    {
        boolean lackOfTime = group != null && group.isTimeCompulsory() && getConsumedDate() == -1;
        boolean lackOfNote = group != null && group.isNoteCompulsory() && getNote().isEmpty();
        return getCategory() == null || amount == 0 || lackOfTime || lackOfNote;
    }

    public boolean needToSync()
    {
        return getLocalUpdatedDate() > getServerUpdatedDate();
    }

    public boolean canBeSubmitWithReport()
    {
        return serverID != -1 && !hasUnuploadedInvoice();
    }

    public boolean containsCategory(List<Category> categories)
    {
        if (categories == null || getCategory() == null)
        {
            return false;
        }

        for (Category category : categories)
        {
            if (getCategory().getServerID() == category.getServerID())
            {
                return true;
            }
        }
        return false;
    }

    public boolean containsSpecificTags(List<Tag> tagList)
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

    public boolean hasInvoice()
    {
        return getInvoices() != null && !getInvoices().isEmpty();
    }

    public boolean hasUnuploadedInvoice()
    {
        if (getInvoices() == null || getInvoices().isEmpty())
        {
            return false;
        }

        for (Image image : getInvoices())
        {
            if (image.isNotUploaded())
            {
                return true;
            }
        }
        return false;
    }

    public boolean hasUnsyncedAttributes()
    {
        return Utils.stringToIntList(getTagsID()).size() != getTags().size() ||
                Utils.stringToIntList(getRelevantUsersID()).size() != getRelevantUsers().size() ||
                !DBManager.getDBManager().isCategoryInDatabase(getCategory().getServerID());
    }

    public static ArrayList<Integer> getItemsIDList(List<Item> items)
    {
        ArrayList<Integer> result = new ArrayList<>();
        if (items == null || items.isEmpty())
        {
            return result;
        }

        for (Item item : items)
        {
            result.add(item.getLocalID());
        }
        return result;
    }

    public static void sortByConsumedDate(List<Item> itemList)
    {
        Collections.sort(itemList, new Comparator<Item>()
        {
            public int compare(Item item1, Item item2)
            {
                return item2.getConsumedDate() - item1.getConsumedDate();
            }
        });
    }

    public static void sortByAmount(List<Item> itemList)
    {
        Collections.sort(itemList, new Comparator<Item>()
        {
            public int compare(Item item1, Item item2)
            {
                double amount1 = item1.getAmount();
                if (!item1.getCurrency().isCNY())
                {
                    amount1 *= item1.getRate() != 0 ? item1.getRate() : item1.getCurrency().getRate();
                }

                double amount2 = item2.getAmount();
                if (!item2.getCurrency().isCNY())
                {
                    amount2 *= item2.getRate() != 0 ? item2.getRate() : item2.getCurrency().getRate();
                }

                return (int) (amount2 - amount1);
            }
        });
    }

    public static void sortByUpdateDate(List<Item> itemList)
    {
        Collections.sort(itemList, new Comparator<Item>()
        {
            public int compare(Item item1, Item item2)
            {
                return item2.getLocalUpdatedDate() - item1.getLocalUpdatedDate();
            }
        });
    }
}
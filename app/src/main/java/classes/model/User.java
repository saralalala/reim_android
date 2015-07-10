package classes.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import classes.utils.CharacterParser;
import classes.utils.DBManager;
import classes.utils.Utils;

public class User implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int serverID = -1;
    private String email = "";
    private String phone = "";
    private String wechat = "";
    private String didi = "";
    private String didiToken = "";
    private String password = "";
    private String nickname = "";
    private String nicknameInitLetter = "";
    private BankAccount bankAccount;
    private int avatarID = -1;
    private String avatarServerPath = "";
    private String avatarLocalPath = "";
    private int privilege = 0;//
    private boolean isActive = false;
    private boolean isAdmin = false;
    private int groupID = -1;
    private int sobID = 0;
    private String appliedCompany = "";
    private int defaultManagerID = -1;
    private int serverUpdatedDate = -1;
    private int localUpdatedDate = -1;

    public User()
    {

    }

    public User(User user)
    {
        serverID = user.getServerID();
        email = user.getEmail();
        phone = user.getPhone();
        wechat = user.getWeChat();
        didi = user.getDidi();
        didiToken = user.getDidiToken();
        password = user.getPassword();
        nickname = user.getNickname();
        avatarID = user.getAvatarID();
        avatarServerPath = user.getAvatarServerPath();
        avatarLocalPath = user.getAvatarLocalPath();
        privilege = user.getPrivilege();
        isActive = user.isActive();
        isAdmin = user.isAdmin();
        groupID = user.getGroupID();
        sobID = user.getSobID();
        appliedCompany = user.getAppliedCompany();
        defaultManagerID = user.getDefaultManagerID();
        serverUpdatedDate = user.getServerUpdatedDate();
        localUpdatedDate = user.getLocalUpdatedDate();
    }

    public User(JSONObject jObject, int groupID)
    {
        try
        {
            setServerID(Integer.valueOf(jObject.getString("id")));
            setEmail(jObject.getString("email"));
            setPhone(jObject.getString("phone"));
            setWeChat(jObject.optString("weixin_nickname", ""));
            setNickname(jObject.getString("nickname"));
            setIsAdmin(Utils.intToBoolean(jObject.getInt("admin")));
            setDefaultManagerID(jObject.getInt("manager_id"));
            setGroupID(groupID);
            setSobID(jObject.optInt("sob_id", 0));
            setAppliedCompany(jObject.optString("apply"));
            setAvatarServerPath(jObject.getString("apath"));
            setAvatarLocalPath("");
            setIsActive(Utils.intToBoolean(jObject.optInt("active", 0)));
            setLocalUpdatedDate(jObject.getInt("dt"));
            setServerUpdatedDate(jObject.getInt("dt"));
            String imageID = jObject.getString("avatar");
            if (imageID.isEmpty())
            {
                setAvatarID(-1);
            }
            else
            {
                setAvatarID(Integer.valueOf(imageID));
            }

            JSONObject didiObject = jObject.optJSONObject("didi");
            if (didiObject != null)
            {
                setDidi(didiObject.getString("phone"));
                setDidiToken(didiObject.getString("token"));
            }
        }
        catch (NumberFormatException | JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void parse(JSONObject jObject, int groupID)
    {
        try
        {
            setServerID(jObject.getInt("id"));
            setNickname(jObject.getString("nickname"));
            setEmail(jObject.getString("email"));
            setPhone(jObject.getString("phone"));
            setWeChat(jObject.getString("weixin_nickname"));
            setDefaultManagerID(jObject.getInt("manager_id"));
            setAvatarServerPath(jObject.optString("apath", ""));
            setAvatarLocalPath("");
            setIsAdmin(Utils.intToBoolean(jObject.getInt("admin")));
            setIsActive(Utils.intToBoolean(jObject.optInt("active", 0)));
            setGroupID(groupID);
            setSobID(jObject.optInt("sob_id", 0));
            setAppliedCompany(jObject.getString("apply"));
            setLocalUpdatedDate(jObject.getInt("lastdt"));
            setServerUpdatedDate(jObject.getInt("lastdt"));
            String imageID = jObject.getString("avatar");
            JSONArray jsonArray = jObject.getJSONArray("banks");
            if (jsonArray.length() > 0)
            {
                setBankAccount(new BankAccount(jsonArray.getJSONObject(0)));
            }
            if (imageID.isEmpty())
            {
                setAvatarID(-1);
            }
            else
            {
                setAvatarID(Integer.valueOf(imageID));
            }

            JSONObject didiObject = jObject.optJSONObject("didi");
            if (didiObject != null)
            {
                setDidi(didiObject.getString("phone"));
                setDidiToken(didiObject.getString("token"));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getServerID()
    {
        return serverID;
    }
    public void setServerID(int serverID)
    {
        this.serverID = serverID;
    }

    public String getEmail()
    {
        return email;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhone()
    {
        return phone;
    }
    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getWeChat()
    {
        return wechat;
    }
    public void setWeChat(String wechat)
    {
        this.wechat = wechat;
    }

    public String getContact()
    {
        return getPhone().isEmpty() ? getEmail() : getPhone();
    }

    public String getDidi()
    {
        return didi;
    }
    public void setDidi(String didi)
    {
        this.didi = didi;
    }

    public String getDidiToken()
    {
        return didiToken;
    }
    public void setDidiToken(String didiToken)
    {
        this.didiToken = didiToken;
    }

    public String getPassword()
    {
        return password;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getNickname()
    {
        return nickname;
    }
    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    public String getNicknameInitLetter()
    {
        return nicknameInitLetter;
    }
    public void setNicknameInitLetter(String nicknameInitLetter)
    {
        this.nicknameInitLetter = nicknameInitLetter;
    }

    public BankAccount getBankAccount()
    {
        return bankAccount;
    }
    public void setBankAccount(BankAccount bankAccount)
    {
        this.bankAccount = bankAccount;
    }

    public int getAvatarID()
    {
        return avatarID;
    }
    public void setAvatarID(int avatarID)
    {
        this.avatarID = avatarID;
    }

    public String getAvatarServerPath()
    {
        return avatarServerPath;
    }
    public void setAvatarServerPath(String avatarServerPath)
    {
        this.avatarServerPath = avatarServerPath;
    }

    public String getAvatarLocalPath()
    {
        return avatarLocalPath;
    }
    public void setAvatarLocalPath(String avatarLocalPath)
    {
        this.avatarLocalPath = avatarLocalPath;
    }

    public int getPrivilege()
    {
        return privilege;
    }
    public void setPrivilege(int privilege)
    {
        this.privilege = privilege;
    }

    public boolean isActive()
    {
        return isActive;
    }
    public void setIsActive(boolean isActive)
    {
        this.isActive = isActive;
    }

    public boolean isAdmin()
    {
        return isAdmin;
    }
    public void setIsAdmin(boolean isAdmin)
    {
        this.isAdmin = isAdmin;
    }

    public int getGroupID()
    {
        return groupID;
    }
    public void setGroupID(int groupID)
    {
        this.groupID = groupID;
    }

    public int getSobID()
    {
        return sobID;
    }
    public void setSobID(int sobID)
    {
        this.sobID = sobID;
    }

    public String getAppliedCompany()
    {
        return appliedCompany;
    }
    public void setAppliedCompany(String appliedCompany)
    {
        this.appliedCompany = appliedCompany;
    }

    public int getDefaultManagerID()
    {
        return defaultManagerID;
    }
    public void setDefaultManagerID(int defaultManagerID)
    {
        this.defaultManagerID = defaultManagerID;
    }
    public User getDefaultManager()
    {
        if (defaultManagerID > 0)
        {
            return DBManager.getDBManager().getUser(defaultManagerID);
        }
        else
        {
            return null;
        }
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

    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }

        if (o instanceof User)
        {
            User user = (User) o;
            return user.getServerID() == this.getServerID();
        }
        return super.equals(o);
    }

    public boolean hasUndownloadedAvatar()
    {
        if (getAvatarLocalPath().isEmpty() && getAvatarID() > 0)
        {
            return true;
        }

        if (!getAvatarLocalPath().isEmpty())
        {
            Bitmap bitmap = BitmapFactory.decodeFile(getAvatarLocalPath());
            if (bitmap == null)
            {
                return true;
            }
        }
        return false;
    }

    public List<User> buildBaseManagerList()
    {
        List<User> tempList = new ArrayList<>();
        User defaultManager = getDefaultManager();
        if (defaultManager != null)
        {
            tempList.add(defaultManager);
        }
        return tempList;
    }

    public static String[] getUsersName(List<User> userList)
    {
        String[] userNames = new String[userList.size()];
        for (int i = 0; i < userList.size(); i++)
        {
            userNames[i] = userList.get(i).getNickname();
        }
        return userNames;
    }

    public static String getUsersNameString(List<User> userList)
    {
        if (userList == null || userList.isEmpty())
        {
            return "";
        }

        return TextUtils.join("、", getUsersName(userList));
    }

    public static String getUsersIDString(List<User> userList)
    {
        if (userList == null || userList.isEmpty())
        {
            return "";
        }

        Integer[] userIDs = new Integer[userList.size()];
        for (int i = 0; i < userList.size(); i++)
        {
            userIDs[i] = userList.get(i).getServerID();
        }

        return TextUtils.join(",", userIDs);
    }

    public static List<User> idStringToUserList(String idString)
    {
        List<User> userList = new ArrayList<>();
        DBManager dbManager = DBManager.getDBManager();
        List<Integer> idList = Utils.stringToIntList(idString);
        for (Integer integer : idList)
        {
            User user = dbManager.getUser(integer);
            if (user != null)
            {
                userList.add(user);
            }
        }
        return userList;
    }

    public static List<User> removeUserFromList(List<User> userList, int userID)
    {
        if (userList == null || userList.isEmpty())
        {
            return new ArrayList<>();
        }

        List<User> tempList = new ArrayList<>();
        for (User user : userList)
        {
            if (user.getServerID() != userID)
            {
                tempList.add(user);
            }
        }

        return tempList;
    }

    public static List<User> filterList(List<User> userList, String keyword)
    {
        List<User> resultList = new ArrayList<>();
        keyword = keyword.toLowerCase();

        for (User user : userList)
        {
            if (user.getNickname().toLowerCase().contains(keyword) ||
                    user.getEmail().toLowerCase().contains(keyword) ||
                    user.getPhone().toLowerCase().contains(keyword))
            {
                resultList.add(user);
            }
        }

        return resultList;
    }

    public static void sortByNickname(List<User> userList)
    {
        for (User user : userList)
        {
            user.setNicknameInitLetter(CharacterParser.getInitLetter(user.getNickname()));
        }

        Collections.sort(userList, new Comparator<User>()
        {
            public int compare(User lhs, User rhs)
            {
                if (!lhs.getNicknameInitLetter().equals("#") && rhs.getNicknameInitLetter().equals("#"))
                {
                    return 1;
                }
                else if (lhs.getNicknameInitLetter().equals("#") && !rhs.getNicknameInitLetter().equals("#"))
                {
                    return -1;
                }
                else
                {
                    return lhs.getNicknameInitLetter().compareTo(rhs.getNicknameInitLetter());
                }
            }
        });
    }

    public static int indexOfContactList(List<User> userList, User targetUser)
    {
        for (int i = 0; i < userList.size(); i++)
        {
            User user = userList.get(i);
            String contact = user.getContact();
            String targetContact = targetUser.getContact();
            if (contact.equals(targetContact) && user.getNickname().equals(targetUser.getNickname()))
            {
                return i;
            }
        }
        return -1;
    }
}

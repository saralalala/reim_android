package classes.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import classes.model.BankAccount;
import classes.model.Category;
import classes.model.Comment;
import classes.model.Currency;
import classes.model.Group;
import classes.model.Image;
import classes.model.Item;
import classes.model.Report;
import classes.model.SetOfBook;
import classes.model.Tag;
import classes.model.User;

public class DBManager extends SQLiteOpenHelper
{
    private static DBManager dbManager = null;
    private static SQLiteDatabase database = null;

    private static final String DATABASE_NAME = "reim.db";
    private static final int DATABASE_VERSION = 7;

    private DBManager(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db)
    {
        try
        {
            String createGroupTable = "CREATE TABLE IF NOT EXISTS tbl_group ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "group_name TEXT DEFAULT(''),"
                    + "group_domain TEXT DEFAULT(''),"
                    + "creator_id INT DEFAULT(0),"
                    + "server_updatedt INT DEFAULT(0),"
                    + "local_updatedt INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createGroupTable);

            String createUserTable = "CREATE TABLE IF NOT EXISTS tbl_user ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "email TEXT DEFAULT(''),"
                    + "phone TEXT DEFAULT(''),"
                    + "wechat TEXT DEFAULT(''),"
                    + "didi TEXT DEFAULT(''),"
                    + "didi_token TEXT DEFAULT(''),"
                    + "nickname TEXT DEFAULT(''),"
                    + "avatar_id INT DEFAULT(0),"
                    + "avatar_server_path TEXT DEFAULT(''),"
                    + "avatar_local_path TEXT DEFAULT(''),"
                    + "privilege INT DEFAULT(0),"
                    + "manager_id INT DEFAULT(0),"
                    + "group_id INT DEFAULT(0),"
                    + "applied_company TEXT DEFAULT(''),"
                    + "admin INT DEFAULT(0),"
                    + "active INT DEFAULT(0),"
                    + "server_updatedt INT DEFAULT(0),"
                    + "local_updatedt INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createUserTable);

            String createItemTable = "CREATE TABLE IF NOT EXISTS tbl_item ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "type INT DEFAULT(0),"
                    + "vendor TEXT DEFAULT(''),"
                    + "currency TEXT DEFAULT('CNY'),"
                    + "rate FLOAT DEFAULT(0),"
                    + "didi_id INT DEFAULT(0),"
                    + "report_local_id INT DEFAULT(0),"
                    + "category_id INT DEFAULT(0),"
                    + "amount FLOAT DEFAULT(0),"
                    + "pa_amount FLOAT DEFAULT(0),"
                    + "user_id INT DEFAULT(0),"
                    + "consumed_date INT DEFAULT(0),"
                    + "note TEXT DEFAULT(''),"
                    + "need_reimbursed INT DEFAULT(0),"
                    + "aa_approved INT DEFAULT(0),"
                    + "status INT DEFAULT(0),"
                    + "location TEXT DEFAULT(''),"
                    + "createdt INT DEFAULT(0),"
                    + "server_updatedt INT DEFAULT(0),"
                    + "local_updatedt INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createItemTable);

            String createItemUserTable = "CREATE TABLE IF NOT EXISTS tbl_item_user ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "item_local_id INT DEFAULT(0),"
                    + "user_id INT DEFAULT(0),"
                    + "local_updatedt INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createItemUserTable);

            String createItemTagTable = "CREATE TABLE IF NOT EXISTS tbl_item_tag ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "item_local_id INT DEFAULT(0),"
                    + "tag_id INT DEFAULT(0),"
                    + "local_updatedt INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createItemTagTable);

            String createOthersItemTable = "CREATE TABLE IF NOT EXISTS tbl_others_item ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "type INT DEFAULT(0),"
                    + "vendor TEXT DEFAULT(''),"
                    + "currency TEXT DEFAULT('CNY'),"
                    + "rate FLOAT DEFAULT(0),"
                    + "didi_id INT DEFAULT(0),"
                    + "report_server_id INT DEFAULT(0),"
                    + "category_id INT DEFAULT(0),"
                    + "tags_id TEXT DEFAULT(''),"
                    + "users_id TEXT DEFAULT(''),"
                    + "amount FLOAT DEFAULT(0),"
                    + "pa_amount FLOAT DEFAULT(0),"
                    + "user_id INT DEFAULT(0),"
                    + "consumed_date INT DEFAULT(0),"
                    + "note TEXT DEFAULT(''),"
                    + "need_reimbursed INT DEFAULT(0),"
                    + "aa_approved INT DEFAULT(0),"
                    + "status INT DEFAULT(0),"
                    + "location TEXT DEFAULT(''),"
                    + "createdt INT DEFAULT(0),"
                    + "server_updatedt INT DEFAULT(0),"
                    + "local_updatedt INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createOthersItemTable);

            String createReportTable = "CREATE TABLE IF NOT EXISTS tbl_report ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "title TEXT DEFAULT(''),"
                    + "user_id INT DEFAULT(0),"
                    + "manager_id TEXT DEFAULT(''),"
                    + "cc_id TEXT DEFAULT(''),"
                    + "status INT DEFAULT(0),"
                    + "type INT DEFAULT(0),"
                    + "aa_approved INT DEFAULT(0),"
                    + "created_date INT DEFAULT(0),"
                    + "server_updatedt INT DEFAULT(0),"
                    + "local_updatedt INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createReportTable);

            String createOthersReportTable = "CREATE TABLE IF NOT EXISTS tbl_others_report ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "owner_id INT DEFAULT(0),"
                    + "title TEXT DEFAULT(''),"
                    + "user_id INT DEFAULT(0),"
                    + "manager_id TEXT DEFAULT(''),"
                    + "cc_id TEXT DEFAULT(''),"
                    + "status INT DEFAULT(0),"
                    + "my_decision INT DEFAULT(0),"
                    + "type INT DEFAULT(0),"
                    + "item_count INT DEFAULT(0),"
                    + "amount TEXT DEFAULT(''),"
                    + "is_cc INT DEFAULT(0),"
                    + "step INT DEFAULT(0),"
                    + "created_date INT DEFAULT(0),"
                    + "server_updatedt INT DEFAULT(0),"
                    + "local_updatedt INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createOthersReportTable);

            String createBankTable = "CREATE TABLE IF NOT EXISTS tbl_bank ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "user_id INT DEFAULT(0),"
                    + "name TEXT DEFAULT(''),"
                    + "number TEXT DEFAULT(''),"
                    + "bank_name TEXT DEFAULT(''),"
                    + "location TEXT DEFAULT(''),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createBankTable);

            String createCommentTable = "CREATE TABLE IF NOT EXISTS tbl_comment ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "report_local_id INT DEFAULT(0),"
                    + "user_id INT DEFAULT(0),"
                    + "comment TEXT DEFAULT(''),"
                    + "comment_date INT DEFAULT(0),"
                    + "local_updatedt INT DEFAULT(0),"
                    + "server_updatedt INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createCommentTable);

            String createOthersCommentTable = "CREATE TABLE IF NOT EXISTS tbl_others_comment ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "report_server_id INT DEFAULT(0),"
                    + "user_id INT DEFAULT(0),"
                    + "comment TEXT DEFAULT(''),"
                    + "comment_date INT DEFAULT(0),"
                    + "local_updatedt INT DEFAULT(0),"
                    + "server_updatedt INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createOthersCommentTable);

            String createCategoryTable = "CREATE TABLE IF NOT EXISTS tbl_category ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "category_name TEXT DEFAULT(''),"
                    + "max_limit INT DEFAULT(0),"
                    + "group_id INT DEFAULT(0),"
                    + "parent_id INT DEFAULT(0),"
                    + "sob_id INT DEFAULT(0),"
                    + "icon_id INT DEFAULT(0),"
                    + "type INT DEFAULT(0),"
                    + "server_updatedt INT DEFAULT(0),"
                    + "local_updatedt INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createCategoryTable);

            String createTagTable = "CREATE TABLE IF NOT EXISTS tbl_tag ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "tag_name TEXT DEFAULT(''),"
                    + "group_id INT DEFAULT(0),"
                    + "icon_id INT DEFAULT(0),"
                    + "icon_path TEXT DEFAULT(''),"
                    + "server_updatedt INT DEFAULT(0),"
                    + "local_updatedt INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createTagTable);

            String createImageTable = "CREATE TABLE IF NOT EXISTS tbl_image ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "server_path TEXT DEFAULT(''),"
                    + "local_path TEXT DEFAULT(''),"
                    + "item_local_id INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createImageTable);

            String createOthersImageTable = "CREATE TABLE IF NOT EXISTS tbl_others_image ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "server_path TEXT DEFAULT(''),"
                    + "local_path TEXT DEFAULT(''),"
                    + "item_server_id INT DEFAULT(0),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createOthersImageTable);

            String createCurrencyTable = "CREATE TABLE IF NOT EXISTS tbl_currency ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "code TEXT DEFAULT(''),"
                    + "symbol TEXT DEFAULT(''),"
                    + "rate FLOAT DEFAULT(0)"
                    + ")";
            db.execSQL(createCurrencyTable);

            String createSetOfBookTable = "CREATE TABLE IF NOT EXISTS tbl_sob ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INT DEFAULT(0),"
                    + "user_id TEXT DEFAULT(''),"
                    + "name TEXT DEFAULT(''),"
                    + "backup1 INT DEFAULT(0),"
                    + "backup2 TEXT DEFAULT(''),"
                    + "backup3 TEXT DEFAULT('')"
                    + ")";
            db.execSQL(createSetOfBookTable);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        LogUtils.println("Upgrading from version " + oldVersion + " to " + newVersion, "TaskDBAdapter");

        if (newVersion > oldVersion)
        {
            if (oldVersion < 2)
            {
                String command = "ALTER TABLE tbl_others_report ADD COLUMN step INT DEFAULT(0)";
                db.execSQL(command);
            }

            if (oldVersion < 3)
            {
                String command = "ALTER TABLE tbl_user ADD COLUMN wechat TEXT DEFAULT('')";
                db.execSQL(command);
            }

            if (oldVersion < 4)
            {
                String command = "ALTER TABLE tbl_user ADD COLUMN active INT DEFAULT(0)";
                db.execSQL(command);
            }

            if (oldVersion < 5)
            {
                String command = "ALTER TABLE tbl_user ADD COLUMN didi TEXT DEFAULT('')";
                db.execSQL(command);

                command = "ALTER TABLE tbl_user ADD COLUMN didi_token TEXT DEFAULT('')";
                db.execSQL(command);

                command = "ALTER TABLE tbl_item ADD COLUMN currency TEXT DEFAULT('CNY')";
                db.execSQL(command);

                command = "ALTER TABLE tbl_item ADD COLUMN rate FLOAT DEFAULT(0)";
                db.execSQL(command);

                command = "ALTER TABLE tbl_others_item ADD COLUMN currency TEXT DEFAULT('CNY')";
                db.execSQL(command);

                command = "ALTER TABLE tbl_others_item ADD COLUMN rate FLOAT DEFAULT(0)";
                db.execSQL(command);
            }

            if (oldVersion < 6)
            {
                String command = "ALTER TABLE tbl_item ADD COLUMN didi_id INT DEFAULT(0)";
                db.execSQL(command);

                command = "ALTER TABLE tbl_others_item ADD COLUMN didi_id INT DEFAULT(0)";
                db.execSQL(command);
            }

            if (oldVersion < 7)
            {
                String command = "ALTER TABLE tbl_category ADD COLUMN sob_id INT DEFAULT(0)";
                db.execSQL(command);
            }
        }
        onCreate(db);
    }

    public static synchronized void createDBManager(Context context)
    {
        if (dbManager == null)
        {
            dbManager = new DBManager(context);
            dbManager.openDatabase();
        }
    }

    public static synchronized DBManager getDBManager()
    {
        return dbManager;
    }

    public boolean openDatabase()
    {
        try
        {
            if (database == null)
            {
                database = getWritableDatabase();
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean closeDatabase()
    {
        try
        {
            close();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // Group
    public boolean insertGroup(Group group)
    {
        try
        {
            String sqlString = "INSERT INTO tbl_group (server_id, group_name, local_updatedt, server_updatedt) VALUES (" +
                    "'" + group.getServerID() + "'," +
                    "'" + sqliteEscape(group.getName()) + "'," +
                    "'" + group.getLocalUpdatedDate() + "'," +
                    "'" + group.getServerUpdatedDate() + "')";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteGroup(int groupServerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_group WHERE server_id = '" + groupServerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateGroup(Group group)
    {
        try
        {
            String sqlString = "UPDATE tbl_group SET " +
                    "group_name = '" + sqliteEscape(group.getName()) + "'," +
                    "local_updatedt = '" + group.getLocalUpdatedDate() + "'," +
                    "server_updatedt = '" + group.getServerUpdatedDate() + "' " +
                    "WHERE server_id = '" + group.getServerID() + "'";

            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public Group getGroup(int groupServerID)
    {
        Cursor cursor = database.rawQuery("SELECT server_id, group_name, local_updatedt, server_updatedt" +
                                                  " FROM tbl_group WHERE server_id = ?",
                                          new String[]{Integer.toString(groupServerID)});
        return getGroupFromCursorWithClose(cursor);
    }

    public boolean syncGroup(Group group)
    {
        try
        {
            Group localGroup = getGroup(group.getServerID());
            if (localGroup == null)
            {
                return insertGroup(group);
            }
            else if (group.getServerUpdatedDate() > localGroup.getLocalUpdatedDate())
            {
                return updateGroup(group);
            }
            else
            {
                return true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // User
    public boolean insertUser(User user)
    {
        try
        {
            String sqlString = "INSERT INTO tbl_user (server_id, email, phone, wechat, didi, didi_token, nickname, avatar_id, avatar_server_path, " +
                    "avatar_local_path, privilege, manager_id, group_id, applied_company, admin, active, local_updatedt, server_updatedt) VALUES (" +
                    "'" + user.getServerID() + "'," +
                    "'" + user.getEmail() + "'," +
                    "'" + user.getPhone() + "'," +
                    "'" + user.getWeChat() + "'," +
                    "'" + user.getDidi() + "'," +
                    "'" + user.getDidiToken() + "'," +
                    "'" + sqliteEscape(user.getNickname()) + "'," +
                    "'" + user.getAvatarID() + "'," +
                    "'" + user.getAvatarServerPath() + "'," +
                    "'" + user.getAvatarLocalPath() + "'," +
                    "'" + user.getPrivilege() + "'," +
                    "'" + user.getDefaultManagerID() + "'," +
                    "'" + user.getGroupID() + "'," +
                    "'" + user.getAppliedCompany() + "'," +
                    "'" + Utils.booleanToInt(user.isAdmin()) + "'," +
                    "'" + Utils.booleanToInt(user.isActive()) + "'," +
                    "'" + user.getLocalUpdatedDate() + "'," +
                    "'" + user.getServerUpdatedDate() + "')";
            database.execSQL(sqlString);

            if (user.getBankAccount() != null)
            {
                deleteBankAccount(user.getServerID());
                insertBankAccount(user.getBankAccount(), user.getServerID());
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int userServerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_user WHERE server_id = '" + userServerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User user)
    {
        try
        {
            String sqlString = "UPDATE tbl_user SET " +
                    "server_id = '" + user.getServerID() + "'," +
                    "email = '" + user.getEmail() + "'," +
                    "phone = '" + user.getPhone() + "'," +
                    "wechat = '" + user.getWeChat() + "'," +
                    "didi = '" + user.getDidi() + "'," +
                    "didi_token = '" + user.getDidiToken() + "'," +
                    "nickname = '" + sqliteEscape(user.getNickname()) + "'," +
                    "avatar_id = '" + user.getAvatarID() + "'," +
                    "avatar_server_path = '" + user.getAvatarServerPath() + "'," +
                    "avatar_local_path = '" + user.getAvatarLocalPath() + "'," +
                    "manager_id = '" + user.getDefaultManagerID() + "'," +
                    "group_id = '" + user.getGroupID() + "'," +
                    "applied_company = '" + user.getAppliedCompany() + "'," +
                    "admin = '" + Utils.booleanToInt(user.isAdmin()) + "'," +
                    "active = '" + Utils.booleanToInt(user.isActive()) + "'," +
                    "local_updatedt = '" + user.getLocalUpdatedDate() + "'," +
                    "server_updatedt = '" + user.getServerUpdatedDate() + "' " +
                    "WHERE server_id = '" + user.getServerID() + "'";
            database.execSQL(sqlString);

            if (user.getBankAccount() != null)
            {
                deleteBankAccount(user.getServerID());
                insertBankAccount(user.getBankAccount(), user.getServerID());
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public User getUser(int userServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_user WHERE server_id = ?",
                                          new String[]{Integer.toString(userServerID)});
        return getUserFromCursorWithClose(cursor);
    }

    public boolean syncUser(User user)
    {
        try
        {
            User localUser = getUser(user.getServerID());
            if (localUser == null)
            {
                return insertUser(user);
            }
            else if (user.getServerUpdatedDate() > localUser.getLocalUpdatedDate())
            {
                if (user.getAvatarID() == localUser.getAvatarID())
                {
                    user.setAvatarLocalPath(localUser.getAvatarLocalPath());
                }
                return updateUser(user);
            }
            else
            {
                return true;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean deleteGroupUsers(int groupServerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_user WHERE group_id = '" + groupServerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateGroupUsers(List<User> userList, int groupServerID)
    {
        try
        {
            List<User> userLocalList = getGroupUsers(AppPreference.getAppPreference().getCurrentGroupID());
            for (User localUser : userLocalList)
            {
                for (User user : userList)
                {
                    if (localUser.equals(user) && localUser.getAvatarID() == user.getAvatarID())
                    {
                        user.setAvatarLocalPath(localUser.getAvatarLocalPath());
                        break;
                    }
                }
            }

            deleteGroupUsers(groupServerID);
            for (User user : userList)
            {
                syncUser(user);
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public List<User> getGroupUsers(int groupServerID)
    {
        List<User> userList = new ArrayList<>();
        Cursor cursor = null;
        try
        {
            if (groupServerID != -1 && groupServerID != 0)
            {
                cursor = database.rawQuery("SELECT * FROM tbl_user WHERE group_id = ?",
                                              new String[]{Integer.toString(groupServerID)});
                while (cursor.moveToNext())
                {
                    userList.add(getUserFromCursor(cursor));
                }
            }
            else
            {
                User user = AppPreference.getAppPreference().getCurrentUser();
                if (user != null)
                {
                    userList.add(user);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return userList;
    }

    public boolean insertRelevantUsers(Item item)
    {
        try
        {
            if (item != null)
            {
                int count = item.getRelevantUsers().size();
                for (int i = 0; i < count; i++)
                {
                    String sqlString = "INSERT INTO tbl_item_user (item_local_id, user_id, local_updatedt) VALUES (" +
                            "'" + item.getLocalID() + "'," +
                            "'" + item.getRelevantUsers().get(i).getServerID() + "'," +
                            "'" + Utils.getCurrentTime() + "')";
                    database.execSQL(sqlString);
                }
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean deleteRelevantUsers(int itemLocalID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_item_user WHERE item_local_id = '" + itemLocalID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRelevantUsers(Item item)
    {
        try
        {
            deleteRelevantUsers(item.getLocalID());
            insertRelevantUsers(item);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getRelevantUsers(int itemLocalID)
    {
        List<User> userList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT user_id FROM tbl_item_user WHERE item_local_id = ?",
                                              new String[]{Integer.toString(itemLocalID)});
        try
        {
            while (cursor.moveToNext())
            {
                User user = getUser(getIntFromCursor(cursor, "user_id"));
                if (user != null)
                {
                    userList.add(user);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return userList;
    }

    // Item
    public int insertItem(Item item)
    {
        try
        {
            LogUtils.println("insert item: local id = " + item.getLocalID() + ", server id = " + item.getServerID());
            int reportID = item.getBelongReport() == null ? -1 : item.getBelongReport().getLocalID();
            int categoryID = item.getCategory() == null ? -1 : item.getCategory().getServerID();
            String currencyCode = item.getCurrency() == null ? "CNY" : item.getCurrency().getCode();
            String sqlString = "INSERT INTO tbl_item (server_id, vendor, report_local_id, category_id, amount, " +
                    "pa_amount, user_id, consumed_date, note, status, location, currency, rate, didi_id, createdt, " +
                    "server_updatedt, local_updatedt, type, need_reimbursed, aa_approved) VALUES (" +
                    "'" + item.getServerID() + "'," +
                    "'" + sqliteEscape(item.getVendor()) + "'," +
                    "'" + reportID + "'," +
                    "'" + categoryID + "'," +
                    "'" + item.getAmount() + "'," +
                    "'" + item.getAaAmount() + "'," +
                    "'" + item.getConsumer().getServerID() + "'," +
                    "'" + item.getConsumedDate() + "'," +
                    "'" + sqliteEscape(item.getNote()) + "'," +
                    "'" + item.getStatus() + "'," +
                    "'" + sqliteEscape(item.getLocation()) + "'," +
                    "'" + currencyCode + "'," +
                    "'" + item.getRate() + "'," +
                    "'" + item.getDidiID() + "'," +
                    "'" + item.getCreatedDate() + "'," +
                    "'" + item.getServerUpdatedDate() + "'," +
                    "'" + item.getLocalUpdatedDate() + "'," +
                    "'" + item.getType() + "'," +
                    "'" + Utils.booleanToInt(item.needReimbursed()) + "'," +
                    "'" + Utils.booleanToInt(item.isAaApproved()) + "')";
            database.execSQL(sqlString);

            Cursor cursor = database.rawQuery("SELECT last_insert_rowid() from tbl_item", null);
            cursor.moveToFirst();
            item.setLocalID(cursor.getInt(0));

            updateItemImages(item);
            updateItemTags(item);
            updateRelevantUsers(item);

            cursor.close();
            return item.getLocalID();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean insertOthersItem(Item item)
    {
        try
        {
            int categoryID = item.getCategory() == null ? -1 : item.getCategory().getServerID();
            String currencyCode = item.getCurrency() == null ? "CNY" : item.getCurrency().getCode();
            String sqlString = "INSERT INTO tbl_others_item (server_id, vendor, report_server_id, category_id, tags_id, " +
                    "users_id, amount, pa_amount, user_id, consumed_date, note, status, location, currency, rate, didi_id, " +
                    "createdt, server_updatedt, local_updatedt, type, need_reimbursed, aa_approved) VALUES (" +
                    "'" + item.getServerID() + "'," +
                    "'" + sqliteEscape(item.getVendor()) + "'," +
                    "'" + item.getBelongReport().getServerID() + "'," +
                    "'" + categoryID + "'," +
                    "'" + item.getTagsID() + "'," +
                    "'" + item.getRelevantUsersID() + "'," +
                    "'" + item.getAmount() + "'," +
                    "'" + item.getAaAmount() + "'," +
                    "'" + item.getConsumer().getServerID() + "'," +
                    "'" + item.getConsumedDate() + "'," +
                    "'" + sqliteEscape(item.getNote()) + "'," +
                    "'" + item.getStatus() + "'," +
                    "'" + sqliteEscape(item.getLocation()) + "'," +
                    "'" + currencyCode + "'," +
                    "'" + item.getRate() + "'," +
                    "'" + item.getDidiID() + "'," +
                    "'" + item.getCreatedDate() + "'," +
                    "'" + item.getServerUpdatedDate() + "'," +
                    "'" + item.getLocalUpdatedDate() + "'," +
                    "'" + item.getType() + "'," +
                    "'" + Utils.booleanToInt(item.needReimbursed()) + "'," +
                    "'" + Utils.booleanToInt(item.isAaApproved()) + "')";
            database.execSQL(sqlString);

            updateOthersItemImages(item);

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteItem(int itemLocalID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_item WHERE id = '" + itemLocalID + "'";
            database.execSQL(sqlString);

            deleteItemImages(itemLocalID);
            deleteItemTags(itemLocalID);
            deleteRelevantUsers(itemLocalID);

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean deleteTrashItems(List<Integer> remainingList, int userServerID)
    {
        try
        {
            String idString = !remainingList.isEmpty() ? TextUtils.join(",", remainingList) + ", -1" : "-1";
            List<Integer> itemIDList = new ArrayList<>();

            String command = "SELECT id FROM tbl_item WHERE server_id NOT IN (" + idString + ") AND user_id = " + userServerID;
            Cursor cursor = database.rawQuery(command, null);

            while (cursor.moveToNext())
            {
                itemIDList.add(getIntFromCursor(cursor, "id"));
            }

            cursor.close();

            for (Integer itemLocalID : itemIDList)
            {
                deleteItem(itemLocalID);
            }

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean updateItem(Item item)
    {
        if (item.getServerID() != -1)
        {
            return updateItemByServerID(item);
        }
        else
        {
            return updateItemByLocalID(item);
        }
    }

    public boolean updateItemByLocalID(Item item)
    {
        try
        {
            LogUtils.println("update item by local id: local id = " + item.getLocalID() + ", server id = " + item.getServerID());
            int reportID = item.getBelongReport() == null ? -1 : item.getBelongReport().getLocalID();
            int categoryID = item.getCategory() == null ? -1 : item.getCategory().getServerID();
            String currencyCode = item.getCurrency() == null ? "CNY" : item.getCurrency().getCode();
            String sqlString = "UPDATE tbl_item SET " +
                    "server_id = '" + item.getServerID() + "'," +
                    "vendor = '" + sqliteEscape(item.getVendor()) + "'," +
                    "report_local_id = '" + reportID + "'," +
                    "category_id = '" + categoryID + "'," +
                    "amount = '" + item.getAmount() + "'," +
                    "pa_amount = '" + item.getAaAmount() + "'," +
                    "user_id = '" + item.getConsumer().getServerID() + "'," +
                    "consumed_date = '" + item.getConsumedDate() + "'," +
                    "note = '" + sqliteEscape(item.getNote()) + "'," +
                    "status = '" + item.getStatus() + "'," +
                    "location = '" + sqliteEscape(item.getLocation()) + "'," +
                    "currency = '" + currencyCode + "'," +
                    "rate = '" + item.getRate() + "'," +
                    "didi_id = '" + item.getDidiID() + "'," +
                    "createdt = '" + item.getCreatedDate() + "'," +
                    "server_updatedt = '" + item.getServerUpdatedDate() + "'," +
                    "local_updatedt = '" + item.getLocalUpdatedDate() + "'," +
                    "type = '" + item.getType() + "'," +
                    "need_reimbursed = '" + Utils.booleanToInt(item.needReimbursed()) + "'," +
                    "aa_approved = '" + Utils.booleanToInt(item.isAaApproved()) + "' " +
                    "WHERE id = '" + item.getLocalID() + "'";
            database.execSQL(sqlString);

            updateItemImages(item);
            updateItemTags(item);
            updateRelevantUsers(item);

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean updateItemByServerID(Item item)
    {
        try
        {
            LogUtils.println("update item by server id: local id = " + item.getLocalID() + ", server id = " + item.getServerID());
            int reportID = item.getBelongReport() == null ? -1 : item.getBelongReport().getLocalID();
            int categoryID = item.getCategory() == null ? -1 : item.getCategory().getServerID();
            String currencyCode = item.getCurrency() == null ? "CNY" : item.getCurrency().getCode();
            String sqlString = "UPDATE tbl_item SET " +
                    "server_id = '" + item.getServerID() + "'," +
                    "vendor = '" + sqliteEscape(item.getVendor()) + "'," +
                    "report_local_id = '" + reportID + "'," +
                    "category_id = '" + categoryID + "'," +
                    "amount = '" + item.getAmount() + "'," +
                    "pa_amount = '" + item.getAaAmount() + "'," +
                    "user_id = '" + item.getConsumer().getServerID() + "'," +
                    "consumed_date = '" + item.getConsumedDate() + "'," +
                    "note = '" + sqliteEscape(item.getNote()) + "'," +
                    "status = '" + item.getStatus() + "'," +
                    "location = '" + sqliteEscape(item.getLocation()) + "'," +
                    "currency = '" + currencyCode + "'," +
                    "rate = '" + item.getRate() + "'," +
                    "didi_id = '" + item.getDidiID() + "'," +
                    "createdt = '" + item.getCreatedDate() + "'," +
                    "server_updatedt = '" + item.getServerUpdatedDate() + "'," +
                    "local_updatedt = '" + item.getLocalUpdatedDate() + "'," +
                    "type = '" + item.getType() + "'," +
                    "need_reimbursed = '" + Utils.booleanToInt(item.needReimbursed()) + "'," +
                    "aa_approved = '" + Utils.booleanToInt(item.isAaApproved()) + "' " +
                    "WHERE server_id = '" + item.getServerID() + "'";
            database.execSQL(sqlString);

            if (item.getInvoices() != null || item.getTags() != null || item.getRelevantUsers() != null)
            {
                item.setLocalID(getItemByServerID(item.getServerID()).getLocalID());

                updateItemImages(item);
                updateItemTags(item);
                updateRelevantUsers(item);
            }

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean updateOthersItem(Item item)
    {
        try
        {
            LogUtils.println("update others item by server id: local id = " + item.getLocalID() + ", server id = " + item.getServerID());

            int categoryID = item.getCategory() == null ? -1 : item.getCategory().getServerID();

            String sqlString = "UPDATE tbl_others_item SET " +
                    "vendor = '" + sqliteEscape(item.getVendor()) + "'," +
                    "category_id = '" + categoryID + "'," +
                    "amount = '" + item.getAmount() + "'," +
                    "consumed_date = '" + item.getConsumedDate() + "'," +
                    "note = '" + sqliteEscape(item.getNote()) + "'," +
                    "location = '" + sqliteEscape(item.getLocation()) + "'," +
                    "tags_id = '" + item.getTagsID() + "'," +
                    "users_id = '" + item.getRelevantUsersID() + "'," +
                    "server_updatedt = '" + item.getServerUpdatedDate() + "'," +
                    "local_updatedt = '" + item.getLocalUpdatedDate() + "' " +
                    "WHERE server_id = '" + item.getServerID() + "'";
            database.execSQL(sqlString);

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public Item getItemByLocalID(int itemLocalID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE id = ?",
                                          new String[]{Integer.toString(itemLocalID)});
        return getItemFromCursorWithClose(cursor);
    }

    public Item getItemByServerID(int itemServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE server_id = ?",
                                          new String[]{Integer.toString(itemServerID)});
        return getItemFromCursorWithClose(cursor);
    }

    public Item getOthersItem(int itemServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_item WHERE server_id = ?",
                                          new String[]{Integer.toString(itemServerID)});
        return getOthersItemFromCursorWithClose(cursor);
    }

    public boolean syncItem(Item item)
    {
        try
        {
            if (item.getLocalID() == -1 && item.getServerID() == -1)
            {
                return Utils.intToBoolean(insertItem(item));
            }
            else if (item.getLocalID() != -1 && item.getServerID() == -1)
            {
                return updateItemByLocalID(item);
            }
            else if (item.getLocalID() == -1) // item.getServerID() != -1
            {
                Item localItem = getItemByServerID(item.getServerID());
                if (localItem == null)
                {
                    return Utils.intToBoolean(insertItem(item));
                }
                else if (item.getServerUpdatedDate() > localItem.getLocalUpdatedDate())
                {
                    return updateItemByServerID(item);
                }
                else
                {
                    return true;
                }
            }
            else // item.getLocalID() != -1 && item.getServerID() != -1
            {
                return updateItemByLocalID(item);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public List<Item> getUserItems(int userServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE user_id = ?",
                                          new String[]{Integer.toString(userServerID)});
        return getItemListFromCursorWithClose(cursor);
    }

    public List<Item> getUnarchivedConsumedItems(int userServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE user_id = ? AND " +
                                                  "(report_local_id = -1 OR report_local_id = 0) AND " +
                                                  "(type = 0 OR (type = 1 AND aa_approved = 1) OR (type = 2 AND aa_approved = 1))",
                                          new String[]{Integer.toString(userServerID)});
        return getItemListFromCursorWithClose(cursor);
    }

    public List<Item> getUnarchivedBudgetItems(int userServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE user_id = ? AND " +
                                                  "(report_local_id = -1 OR report_local_id = 0) AND " +
                                                  "type = 1 AND aa_approved = 0",
                                          new String[]{Integer.toString(userServerID)});
        return getItemListFromCursorWithClose(cursor);
    }

    public List<Item> getUnarchivedBorrowingItems(int userServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE user_id = ? AND " +
                                                  "(report_local_id = -1 OR report_local_id = 0) AND " +
                                                  "type = 2 AND aa_approved = 0",
                                          new String[]{Integer.toString(userServerID)});
        return getItemListFromCursorWithClose(cursor);
    }

    public List<Item> getUnsyncedItems(int userServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE local_updatedt > server_updatedt AND user_id = ?",
                                          new String[]{Integer.toString(userServerID)});
        return getItemListFromCursorWithClose(cursor);
    }

    public List<Item> getExistsUserItems(int userServerID)
    {
        List<Item> itemList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT id, server_id FROM tbl_item WHERE user_id = ? AND server_id != -1",
                                          new String[]{Integer.toString(userServerID)});
        try
        {
            while (cursor.moveToNext())
            {
                Item item = new Item();
                item.setLocalID(getIntFromCursor(cursor, "id"));
                item.setServerID(getIntFromCursor(cursor, "server_id"));
                itemList.add(item);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return itemList;
    }

    public List<Item> getItems(ArrayList<Integer> chosenItemIDList)
    {
        List<Item> itemList = new ArrayList<>();
        try
        {
            for (int i = 0; i < chosenItemIDList.size(); i++)
            {
                itemList.add(getItemByLocalID(chosenItemIDList.get(i)));
            }
            return itemList;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return itemList;
        }
    }

    public boolean syncItemList(List<Item> itemList, int userServerID)
    {
        try
        {
            List<Item> itemLocalList = getExistsUserItems(userServerID);
            for (int i = 0; i < itemList.size(); i++)
            {
                Item item = itemList.get(i);
                boolean itemExists = false;
                for (int j = 0; j < itemLocalList.size(); j++)
                {
                    Item localItem = itemLocalList.get(j);
                    if (item.getServerID() == localItem.getServerID())
                    {
                        item.setLocalID(localItem.getLocalID());
                        updateItemByLocalID(item);
                        itemExists = true;
                        break;
                    }
                }
                if (!itemExists)
                {
                    insertItem(item);
                }
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertReportItem(int reportLocalID, int itemLocalID)
    {
        try
        {
            String sqlString = "UPDATE tbl_item SET " +
                    "report_local_id = '" + reportLocalID + "' " +
                    "WHERE id = '" + itemLocalID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertReportItems(ArrayList<Integer> itemIDList, int reportLocalID)
    {
        try
        {
            int count = itemIDList.size();
            for (int i = 0; i < count; i++)
            {
                insertReportItem(reportLocalID, itemIDList.get(i));
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateReportItems(ArrayList<Integer> itemIDList, int reportLocalID)
    {
        try
        {
            deleteReportItems(reportLocalID);
            insertReportItems(itemIDList, reportLocalID);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteReportItems(int reportLocalID)
    {
        try
        {
            String sqlString = "UPDATE tbl_item SET " +
                    "report_local_id = '" + -1 + "' " +
                    "WHERE report_local_id = '" + reportLocalID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteOthersReportItems(int reportServerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_others_item WHERE report_server_id = '" + reportServerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public List<Item> getReportItems(int reportLocalID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE report_local_id = ?",
                                          new String[]{Integer.toString(reportLocalID)});
        return getItemListFromCursorWithClose(cursor);
    }

    public List<Item> getOthersReportItems(int reportServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_item WHERE report_server_id = ?",
                                          new String[]{Integer.toString(reportServerID)});
        return getOthersItemListFromCursorWithClose(cursor);
    }

    // Report
    public int insertReport(Report report)
    {
        try
        {
            LogUtils.println("insert report: local id = " + report.getLocalID() + ", server id = " + report.getServerID());
            String sqlString = "INSERT INTO tbl_report (server_id, title, user_id, status, aa_approved, manager_id, cc_id, type, " +
                    "created_date, server_updatedt, local_updatedt) VALUES (" +
                    "'" + report.getServerID() + "'," +
                    "'" + sqliteEscape(report.getTitle()) + "'," +
                    "'" + report.getSender().getServerID() + "'," +
                    "'" + report.getStatus() + "'," +
                    "'" + Utils.booleanToInt(report.isAaApproved()) + "'," +
                    "'" + User.getUsersIDString(report.getManagerList()) + "'," +
                    "'" + User.getUsersIDString(report.getCCList()) + "'," +
                    "'" + report.getType() + "'," +
                    "'" + report.getCreatedDate() + "'," +
                    "'" + report.getServerUpdatedDate() + "'," +
                    "'" + report.getLocalUpdatedDate() + "')";
            database.execSQL(sqlString);

            Cursor cursor = database.rawQuery("SELECT last_insert_rowid() from tbl_report", null);
            cursor.moveToFirst();
            report.setLocalID(cursor.getInt(0));
            cursor.close();

            return report.getLocalID();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean insertOthersReport(Report report)
    {
        try
        {
            String sqlString = "INSERT INTO tbl_others_report (server_id, owner_id, title, user_id, status, my_decision, manager_id, cc_id, " +
                    "type, amount, item_count, is_cc, step, created_date, server_updatedt, local_updatedt) VALUES (" +
                    "'" + report.getServerID() + "'," +
                    "'" + AppPreference.getAppPreference().getCurrentUserID() + "'," +
                    "'" + sqliteEscape(report.getTitle()) + "'," +
                    "'" + report.getSender().getServerID() + "'," +
                    "'" + report.getStatus() + "'," +
                    "'" + report.getMyDecision() + "'," +
                    "'" + User.getUsersIDString(report.getManagerList()) + "'," +
                    "'" + User.getUsersIDString(report.getCCList()) + "'," +
                    "'" + report.getType() + "'," +
                    "'" + report.getAmount() + "'," +
                    "'" + report.getItemCount() + "'," +
                    "'" + Utils.booleanToInt(report.isCC()) + "'," +
                    "'" + report.getStep() + "'," +
                    "'" + report.getCreatedDate() + "'," +
                    "'" + report.getServerUpdatedDate() + "'," +
                    "'" + report.getLocalUpdatedDate() + "')";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteReport(int reportLocalID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_report WHERE id = '" + reportLocalID + "'";
            database.execSQL(sqlString);

            deleteReportComments(reportLocalID);
            deleteReportItems(reportLocalID);

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteOthersReport(int reportServerID, int managerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_others_report WHERE server_id = '" + reportServerID + "' AND owner_id = '" + managerID + "'";
            database.execSQL(sqlString);

            dbManager.deleteOthersReportItems(reportServerID);
            dbManager.deleteOthersReportComments(reportServerID);

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteOthersReports(int managerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_others_report WHERE owner_id = '" + managerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTrashReports(List<Integer> remainingList, int userServerID)
    {
        try
        {
            String idString = !remainingList.isEmpty() ? TextUtils.join(",", remainingList) + ",-1" : "-1";
            List<Integer> reportIDList = new ArrayList<>();

            String command = "SELECT id FROM tbl_report WHERE server_id NOT IN (" + idString + ") AND user_id = " + userServerID;
            Cursor cursor = database.rawQuery(command, null);

            while (cursor.moveToNext())
            {
                reportIDList.add(getIntFromCursor(cursor, "id"));
            }

            cursor.close();

            for (Integer reportLocalID : reportIDList)
            {
                deleteReport(reportLocalID);
            }

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean updateReportByLocalID(Report report)
    {
        try
        {
            LogUtils.println("update report by local id: local id = " + report.getLocalID() + ", server id = " + report.getServerID());
            String sqlString = "UPDATE tbl_report SET " +
                    "server_id = '" + report.getServerID() + "'," +
                    "title = '" + sqliteEscape(report.getTitle()) + "'," +
                    "user_id = '" + report.getSender().getServerID() + "'," +
                    "status = '" + report.getStatus() + "'," +
                    "aa_approved = '" + Utils.booleanToInt(report.isAaApproved()) + "'," +
                    "manager_id = '" + User.getUsersIDString(report.getManagerList()) + "'," +
                    "cc_id = '" + User.getUsersIDString(report.getCCList()) + "'," +
                    "type = '" + report.getType() + "'," +
                    "created_date = '" + report.getCreatedDate() + "'," +
                    "server_updatedt = '" + report.getServerUpdatedDate() + "'," +
                    "local_updatedt = '" + report.getLocalUpdatedDate() + "' " +
                    "WHERE id = '" + report.getLocalID() + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateReportByServerID(Report report)
    {
        try
        {
            LogUtils.println("update report by server id: local id = " + report.getLocalID() + ", server id = " + report.getServerID());
            String sqlString = "UPDATE tbl_report SET " +
                    "server_id = '" + report.getServerID() + "'," +
                    "title = '" + sqliteEscape(report.getTitle()) + "'," +
                    "user_id = '" + report.getSender().getServerID() + "'," +
                    "status = '" + report.getStatus() + "'," +
                    "aa_approved = '" + Utils.booleanToInt(report.isAaApproved()) + "'," +
                    "manager_id = '" + User.getUsersIDString(report.getManagerList()) + "'," +
                    "cc_id = '" + User.getUsersIDString(report.getCCList()) + "'," +
                    "type = '" + report.getType() + "'," +
                    "created_date = '" + report.getCreatedDate() + "'," +
                    "server_updatedt = '" + report.getServerUpdatedDate() + "'," +
                    "local_updatedt = '" + report.getLocalUpdatedDate() + "' " +
                    "WHERE server_id = '" + report.getServerID() + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOthersReport(Report report)
    {
        try
        {
            String amountString = report.getAmount() == null ? "" : "amount = '" + report.getAmount() + "',";
            String sqlString = "UPDATE tbl_others_report SET " + amountString +
                    "server_id = '" + report.getServerID() + "'," +
                    "owner_id = '" + AppPreference.getAppPreference().getCurrentUserID() + "'," +
                    "title = '" + sqliteEscape(report.getTitle()) + "'," +
                    "user_id = '" + report.getSender().getServerID() + "'," +
                    "manager_id = '" + User.getUsersIDString(report.getManagerList()) + "'," +
                    "cc_id = '" + User.getUsersIDString(report.getCCList()) + "'," +
                    "status = '" + report.getStatus() + "'," +
                    "my_decision = '" + report.getMyDecision() + "'," +
                    "type = '" + report.getType() + "'," +
                    "is_cc = '" + Utils.booleanToInt(report.isCC()) + "'," +
                    "step = '" + report.getStep() + "'," +
                    "amount = '" + report.getAmount() + "'," +
                    "item_count = '" + report.getItemCount() + "'," +
                    "created_date = '" + report.getCreatedDate() + "'," +
                    "server_updatedt = '" + report.getServerUpdatedDate() + "'," +
                    "local_updatedt = '" + report.getLocalUpdatedDate() + "' " +
                    "WHERE server_id = '" + report.getServerID() + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public Report getReportByLocalID(int reportLocalID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_report WHERE id = ? ",
                                          new String[]{Integer.toString(reportLocalID)});
        return getReportFromCursorWithClose(cursor);
    }

    public Report getReportByServerID(int reportServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_report WHERE server_id = ? ",
                                          new String[]{Integer.toString(reportServerID)});
        return getReportFromCursorWithClose(cursor);
    }

    public Report getOthersReport(int reportServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_report WHERE server_id = ?",
                                          new String[]{Integer.toString(reportServerID)});
        return getOthersReportFromCursorWithClose(cursor);
    }

    public boolean syncReport(Report report)
    {
        try
        {
            if (report.getLocalID() == -1 && report.getServerID() == -1)
            {
                return Utils.intToBoolean(insertReport(report));
            }
            else if (report.getLocalID() != -1 && report.getServerID() == -1)
            {
                return updateReportByLocalID(report);
            }
            else if (report.getLocalID() == -1) // report.getServerID() != -1
            {
                Report localReport = getReportByServerID(report.getServerID());
                if (localReport == null)
                {
                    return Utils.intToBoolean(insertReport(report));
                }
                else if (report.getServerUpdatedDate() > localReport.getLocalUpdatedDate())
                {
                    return updateReportByServerID(report);
                }
                else
                {
                    return true;
                }
            }
            else // report.getLocalID() != -1 && report.getServerID() != -1
            {
                return updateReportByLocalID(report);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public List<Report> getUnsyncedUserReports(int userServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_report WHERE local_updatedt > server_updatedt AND " +
                                                  "(user_id = ? OR manager_id = ?)",
                                          new String[]{Integer.toString(userServerID), Integer.toString(userServerID)});
        return getReportListFromCursorWithClose(cursor);
    }

    public List<Report> getExistsUserReports(int userServerID)
    {
        List<Report> reportList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT id, server_id FROM tbl_report WHERE user_id = ? AND server_id != -1",
                                          new String[]{Integer.toString(userServerID)});
        try
        {
            while (cursor.moveToNext())
            {
                Report report = new Report();
                report.setLocalID(getIntFromCursor(cursor, "id"));
                report.setServerID(getIntFromCursor(cursor, "server_id"));

                reportList.add(report);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return reportList;
    }

    public List<Report> getUserReports(int userServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_report WHERE user_id = ?",
                                          new String[]{Integer.toString(userServerID)});
        return getReportListFromCursorWithClose(cursor);
    }

    public List<Report> getOthersReports(int userServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_report WHERE owner_id = ?",
                                          new String[]{Integer.toString(userServerID)});
        return getOthersReportListFromCursorWithClose(cursor);
    }

    public String getReportItemIDs(int reportLocalID)
    {
        String result = "";
        List<Integer> idList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT server_id FROM tbl_item WHERE report_local_id = ?",
                                          new String[]{Integer.toString(reportLocalID)});
        try
        {
            while (cursor.moveToNext())
            {
                idList.add(getIntFromCursor(cursor, "server_id"));
            }

            result = TextUtils.join(",", idList);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return result;
    }

    public double getReportAmount(int reportLocalID)
    {
        double amount = 0;
        Cursor cursor = database.rawQuery("SELECT currency, amount, rate FROM tbl_item WHERE report_local_id = ?",
                                          new String[]{Integer.toString(reportLocalID)});
        try
        {
            while (cursor.moveToNext())
            {
                String code = getStringFromCursor(cursor, "currency");
                double itemAmount = getDoubleFromCursor(cursor, "amount");
                double itemRate = getDoubleFromCursor(cursor, "rate");

                if (code.equals("CNY"))
                {
                    amount += itemAmount;
                }
                else if (itemRate != 0)
                {
                    amount += itemAmount * itemRate / 100;
                }
                else
                {
                    amount += itemAmount * getRate(code) / 100;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return amount;
    }

    public boolean syncReportList(List<Report> reportList, int userServerID)
    {
        try
        {
            List<Report> reportLocalList = getExistsUserReports(userServerID);
            for (int i = 0; i < reportList.size(); i++)
            {
                Report report = reportList.get(i);
                boolean reportExists = false;
                for (int j = 0; j < reportLocalList.size(); j++)
                {
                    Report localReport = reportLocalList.get(j);
                    if (report.getServerID() == localReport.getServerID())
                    {
                        report.setLocalID(localReport.getLocalID());
                        updateReportByLocalID(report);
                        reportExists = true;
                    }
                }
                if (!reportExists)
                {
                    insertReport(report);
                }
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // Comment
    public boolean insertComment(Comment comment)
    {
        try
        {
            String sqlString = "INSERT INTO tbl_comment (server_id, report_local_id, user_id, comment, comment_date, " +
                    "server_updatedt, local_updatedt) VALUES (" +
                    "'" + comment.getServerID() + "'," +
                    "'" + comment.getReportID() + "'," +
                    "'" + comment.getReviewer().getServerID() + "'," +
                    "'" + sqliteEscape(comment.getContent()) + "'," +
                    "'" + comment.getCreatedDate() + "'," +
                    "'" + comment.getServerUpdatedDate() + "'," +
                    "'" + comment.getLocalUpdatedDate() + "')";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean insertOthersComment(Comment comment)
    {
        try
        {
            String sqlString = "INSERT INTO tbl_others_comment (server_id, report_server_id, user_id, comment, comment_date, " +
                    "server_updatedt, local_updatedt) VALUES (" +
                    "'" + comment.getServerID() + "'," +
                    "'" + comment.getReportID() + "'," +
                    "'" + comment.getReviewer().getServerID() + "'," +
                    "'" + sqliteEscape(comment.getContent()) + "'," +
                    "'" + comment.getCreatedDate() + "'," +
                    "'" + comment.getServerUpdatedDate() + "'," +
                    "'" + comment.getLocalUpdatedDate() + "')";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean deleteComment(int commentLocalID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_comment WHERE id = '" + commentLocalID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteOthersComment(int commentServerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_others_comment WHERE server_id = '" + commentServerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteReportComments(int reportLocalID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_comment WHERE report_local_id = '" + reportLocalID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteOthersReportComments(int reportServerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_others_comment WHERE report_server_id = '" + reportServerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public List<Comment> getReportComments(int reportLocalID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_comment WHERE report_local_id = ?",
                                          new String[]{Integer.toString(reportLocalID)});
        return getCommentListFromCursorWithClose(cursor);
    }

    public List<Comment> getOthersReportComments(int reportServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_comment WHERE report_server_id = ?",
                                          new String[]{Integer.toString(reportServerID)});
        return getOthersCommentListFromCursorWithClose(cursor);
    }

    // Category
    public boolean insertCategory(Category category)
    {
        try
        {
            String sqlString = "INSERT INTO tbl_category (server_id, category_name, max_limit, group_id, " +
                    "parent_id, sob_id, icon_id, type, local_updatedt, server_updatedt) VALUES (" +
                    "'" + category.getServerID() + "'," +
                    "'" + sqliteEscape(category.getName()) + "'," +
                    "'" + category.getLimit() + "'," +
                    "'" + category.getGroupID() + "'," +
                    "'" + category.getParentID() + "'," +
                    "'" + category.getSetOfBookID() + "'," +
                    "'" + category.getIconID() + "'," +
                    "'" + category.getType() + "'," +
                    "'" + category.getLocalUpdatedDate() + "'," +
                    "'" + category.getServerUpdatedDate() + "')";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCategory(int categoryServerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_category WHERE server_id = '" + categoryServerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCategory(Category category)
    {
        try
        {
            String sqlString = "UPDATE tbl_category SET " +
                    "category_name = '" + sqliteEscape(category.getName()) + "'," +
                    "max_limit = '" + category.getLimit() + "'," +
                    "group_id = '" + category.getGroupID() + "'," +
                    "parent_id = '" + category.getParentID() + "'," +
                    "sob_id = '" + category.getSetOfBookID() + "'," +
                    "icon_id = '" + category.getIconID() + "'," +
                    "type = '" + category.getType() + "'," +
                    "local_updatedt = '" + category.getLocalUpdatedDate() + "'," +
                    "server_updatedt = '" + category.getServerUpdatedDate() + "' " +
                    "WHERE server_id = '" + category.getServerID() + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public Category getCategory(int categoryServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_category WHERE server_id = ?",
                                          new String[]{Integer.toString(categoryServerID)});
        return getCategoryFromCursorWithClose(cursor);
    }

    public boolean insertCategoryList(List<Category> categoryList)
    {
        try
        {
            for (Category category : categoryList)
            {
                insertCategory(category);
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean deleteGroupCategories(int groupServerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_category WHERE group_id = '" + groupServerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSubCategories(int categoryServerID, int groupServerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_category WHERE group_id = '" + groupServerID + "' " +
                    "AND parent_id = '" + categoryServerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateGroupCategories(List<Category> categoryList, int groupServerID)
    {
        try
        {
            deleteGroupCategories(groupServerID);
            insertCategoryList(categoryList);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public List<Category> getGroupCategories(int groupServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_category WHERE group_id = ? AND parent_id = 0",
                                          new String[]{Integer.toString(groupServerID)});
        return getCategoryListFromCursorWithClose(cursor);
    }

    public List<Category> getUserCategories(int userID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_category WHERE sob_id IN (?) AND parent_id = 0",
                                          new String[]{getUserSetOfBookIDs(userID)});
        return getCategoryListFromCursorWithClose(cursor);
    }

    public List<Category> getSubCategories(int parentServerID, int groupServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_category WHERE group_id = ? AND parent_id = ?",
                                          new String[]{Integer.toString(groupServerID), Integer.toString(parentServerID)});
        return getCategoryListFromCursorWithClose(cursor);
    }

    // Tag
    public boolean insertTag(Tag tag)
    {
        try
        {
            String sqlString = "INSERT INTO tbl_tag (server_id, tag_name, group_id, icon_id, icon_path, local_updatedt, server_updatedt) VALUES (" +
                    "'" + tag.getServerID() + "'," +
                    "'" + sqliteEscape(tag.getName()) + "'," +
                    "'" + tag.getGroupID() + "'," +
                    "'" + tag.getIconID() + "'," +
                    "'" + tag.getIconPath() + "'," +
                    "'" + tag.getLocalUpdatedDate() + "'," +
                    "'" + tag.getServerUpdatedDate() + "')";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTag(int tagServerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_tag WHERE server_id = '" + tagServerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTag(Tag tag)
    {
        try
        {
            String sqlString = "UPDATE tbl_tag SET " +
                    "tag_name = '" + sqliteEscape(tag.getName()) + "'," +
                    "group_id = '" + tag.getGroupID() + "'," +
                    "icon_id = '" + tag.getIconID() + "'," +
                    "icon_path = '" + tag.getIconPath() + "'," +
                    "local_updatedt = '" + tag.getLocalUpdatedDate() + "'," +
                    "server_updatedt = '" + tag.getServerUpdatedDate() + "' " +
                    "WHERE server_id = '" + tag.getServerID() + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public Tag getTag(int tagServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_tag WHERE server_id = ?",
                                          new String[]{Integer.toString(tagServerID)});
        return getTagFromCursorWithClose(cursor);
    }

    public boolean syncTag(Tag tag)
    {
        try
        {
            Tag localTag = getTag(tag.getServerID());
            if (localTag == null)
            {
                return insertTag(tag);
            }
            else if (tag.getServerUpdatedDate() > localTag.getLocalUpdatedDate())
            {
                return updateTag(tag);
            }
            else
            {
                return true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertTagList(List<Tag> tagList)
    {
        try
        {
            for (int i = 0; i < tagList.size(); i++)
            {
                insertTag(tagList.get(i));
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean deleteGroupTags(int groupServerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_tag WHERE group_id = '" + groupServerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateGroupTags(List<Tag> tagList, int groupServerID)
    {
        try
        {
            deleteGroupTags(groupServerID);
            insertTagList(tagList);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public List<Tag> getGroupTags(int groupServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_tag WHERE group_id = ?",
                                          new String[]{Integer.toString(groupServerID)});
        return getTagListFromCursorWithClose(cursor);
    }

    public boolean insertItemTags(Item item)
    {
        try
        {
            if (item.getTags() != null)
            {
                int count = item.getTags().size();
                for (int i = 0; i < count; i++)
                {
                    String sqlString = "INSERT INTO tbl_item_tag (item_local_id, tag_id, local_updatedt) VALUES (" +
                            "'" + item.getLocalID() + "'," +
                            "'" + item.getTags().get(i).getServerID() + "'," +
                            "'" + Utils.getCurrentTime() + "')";
                    database.execSQL(sqlString);
                }
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean deleteItemTags(int itemLocalID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_item_tag WHERE item_local_id = '" + itemLocalID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateItemTags(Item item)
    {
        try
        {
            deleteItemTags(item.getLocalID());
            insertItemTags(item);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public List<Tag> getItemTags(int itemLocalID)
    {
        List<Tag> tags = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT tag_id FROM tbl_item_tag WHERE item_local_id = ?",
                                             new String[]{Integer.toString(itemLocalID)});
        try
        {
            while (cursor.moveToNext())
            {
                Tag tag = getTag(getIntFromCursor(cursor, "tag_id"));
                if (tag != null)
                {
                    tags.add(tag);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return tags;
    }

    // Image
    public boolean insertImage(Image image)
    {
        try
        {
            String sqlString = "INSERT INTO tbl_image (server_id, server_path, local_path, item_local_id) VALUES (" +
                    "'" + image.getServerID() + "'," +
                    "'" + image.getServerPath() + "'," +
                    "'" + image.getLocalPath() + "'," +
                    "'" + image.getItemID() + "')";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean insertOthersImage(Image image)
    {
        try
        {
            String sqlString = "INSERT INTO tbl_others_image (server_id, server_path, local_path, item_server_id) VALUES (" +
                    "'" + image.getServerID() + "'," +
                    "'" + image.getServerPath() + "'," +
                    "'" + image.getLocalPath() + "'," +
                    "'" + image.getItemID() + "')";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean deleteImage(int imageLocalID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_image WHERE id = '" + imageLocalID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteOthersImage(int imageServerID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_others_image WHERE server_id = '" + imageServerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteItemImages(int itemLocalID)
    {
        try
        {
            List<Image> images = getItemImages(itemLocalID);
            for (Image image : images)
            {
                image.deleteFile();
            }

            String sqlString = "DELETE FROM tbl_image WHERE item_local_id = '" + itemLocalID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteOthersItemImages(int itemServerID)
    {
        try
        {
            List<Image> images = getOthersItemImages(itemServerID);
            for (Image image : images)
            {
                image.deleteFile();
            }

            String sqlString = "DELETE FROM tbl_others_image WHERE item_server_id = '" + itemServerID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateImageServerID(Image image)
    {
        try
        {
            String sqlString = "UPDATE tbl_image SET " +
                    "server_id = '" + image.getServerID() + "' " +
                    "WHERE id = '" + image.getLocalID() + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean updateImageLocalPath(Image image)
    {
        try
        {
            String sqlString = "UPDATE tbl_image SET " +
                    "local_path = '" + image.getLocalPath() + "' " +
                    "WHERE server_id = '" + image.getServerID() + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean updateImageServerPath(Image image)
    {
        try
        {
            String sqlString = "UPDATE tbl_image SET " +
                    "server_path = '" + image.getServerPath() + "' " +
                    "WHERE server_id = '" + image.getServerID() + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean updateOthersImageLocalPath(Image image)
    {
        try
        {
            String sqlString = "UPDATE tbl_others_image SET " +
                    "local_path = '" + image.getLocalPath() + "' " +
                    "WHERE server_id = '" + image.getServerID() + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean updateOthersImageServerPath(Image image)
    {
        try
        {
            String sqlString = "UPDATE tbl_others_image SET " +
                    "server_path = '" + image.getServerPath() + "' " +
                    "WHERE server_id = '" + image.getServerID() + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public void updateItemImages(Item item)
    {
        int itemLocalID = item.getLocalID();
        List<Image> images = item.getInvoices();
        List<Image> localImages = getItemImages(itemLocalID);

        for (Image image : localImages)
        {
            if (!images.contains(image))
            {
                image.deleteFile();
                deleteImage(image.getLocalID());
            }
        }

        localImages = getItemImages(itemLocalID);
        for (Image image : images)
        {
            boolean imageExists = false;
            for (Image localImage : localImages)
            {
                if (localImage.equals(image))
                {
                    imageExists = true;
                    updateImageServerPath(image);
                    break;
                }
            }
            if (!imageExists)
            {
                image.setItemID(itemLocalID);
                insertImage(image);
            }
        }
    }

    public void updateOthersItemImages(Item item)
    {
        int itemServerID = item.getServerID();
        List<Image> images = item.getInvoices();
        List<Image> localImages = getOthersItemImages(itemServerID);

        for (Image image : localImages)
        {
            if (!images.contains(image))
            {
                image.deleteFile();
                deleteImage(image.getLocalID());
            }
        }

        localImages = getOthersItemImages(itemServerID);
        for (Image image : images)
        {
            boolean imageExists = false;
            for (Image localImage : localImages)
            {
                if (localImage.equals(image))
                {
                    imageExists = true;
                    updateOthersImageServerPath(image);
                    break;
                }
            }
            if (!imageExists)
            {
                image.setItemID(itemServerID);
                insertOthersImage(image);
            }
        }
    }

    public Image getImageByLocalID(int imageLocalID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_image WHERE id = ?",
                                          new String[]{Integer.toString(imageLocalID)});
        return getImageFromCursorWithClose(cursor);
    }

    public Image getImageByServerID(int imageServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_image WHERE server_id = ?",
                                          new String[]{Integer.toString(imageServerID)});
        return getImageFromCursorWithClose(cursor);
    }

    public Image getOthersImage(int imageServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_image WHERE server_id = ?",
                                          new String[]{Integer.toString(imageServerID)});
        return getOthersImageFromCursorWithClose(cursor);
    }

    public List<Image> getItemImages(int itemLocalID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_image WHERE item_local_id = ?",
                                          new String[]{Integer.toString(itemLocalID)});
        return getImageListFromCursorWithClose(cursor);
    }

    public List<Image> getOthersItemImages(int itemServerID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_image WHERE item_server_id = ?",
                                          new String[]{Integer.toString(itemServerID)});
        return getOthersImageListFromCursorWithClose(cursor);
    }

    // Currency
    public boolean insertCurrency(Currency currency)
    {
        try
        {
            String sqlString = "INSERT INTO tbl_currency (code, symbol, rate) VALUES (" +
                    "'" + currency.getCode() + "'," +
                    "'" + currency.getSymbol() + "'," +
                    "'" + currency.getRate() + "')";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCurrency(Currency currency)
    {
        try
        {
            String sqlString = "UPDATE tbl_currency SET " +
                    "rate = '" + currency.getRate() + "' " +
                    "WHERE code = '" + currency.getCode() + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public Currency getCurrency(String code)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_currency WHERE code = ?", new String[]{code});
        return getCurrencyFromCursorWithClose(cursor);
    }

    public double getRate(String code)
    {
        Cursor cursor = database.rawQuery("SELECT rate FROM tbl_currency WHERE code = ?", new String[]{code});
        double rate = 0;
        try
        {
            if (cursor.moveToNext())
            {
                rate = getDoubleFromCursor(cursor, "rate");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return rate;
    }

    public boolean updateCurrencyList(List<Currency> currencyList)
    {
        try
        {
            for (Currency currency : currencyList)
            {
                updateCurrency(currency);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public List<Currency> getCurrencyList()
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_currency", null);
        return getCurrencyListFromCursorWithClose(cursor);
    }

    public boolean isCurrencyTableEmpty()
    {
        try
        {
            Cursor cursor = database.rawQuery("SELECT * FROM tbl_currency", null);
            boolean result = !cursor.moveToNext();
            cursor.close();

            return result;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    // Bank Account
    public int insertBankAccount(BankAccount bankAccount, int userID)
    {
        try
        {
            String sqlString = "INSERT INTO tbl_bank (server_id, user_id, name, number, bank_name, location) VALUES (" +
                    "'" + bankAccount.getServerID() + "'," +
                    "'" + userID + "'," +
                    "'" + sqliteEscape(bankAccount.getName()) + "'," +
                    "'" + sqliteEscape(bankAccount.getNumber()) + "'," +
                    "'" + sqliteEscape(bankAccount.getBankName()) + "'," +
                    "'" + sqliteEscape(bankAccount.getLocation()) + "')";
            database.execSQL(sqlString);

            Cursor cursor = database.rawQuery("SELECT last_insert_rowid() from tbl_bank", null);
            cursor.moveToFirst();
            bankAccount.setLocalID(cursor.getInt(0));
            cursor.close();

            return bankAccount.getLocalID();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean deleteBankAccount(int userID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_bank WHERE user_id = '" + userID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBankAccount(BankAccount bankAccount)
    {
        try
        {
            String sqlString = "UPDATE tbl_bank SET " +
                    "server_id = '" + bankAccount.getServerID() + "'," +
                    "name = '" + sqliteEscape(bankAccount.getName()) + "'," +
                    "number = '" + sqliteEscape(bankAccount.getNumber()) + "'," +
                    "bank_name = '" + sqliteEscape(bankAccount.getBankName()) + "'," +
                    "location = '" + sqliteEscape(bankAccount.getLocation()) + "' " +
                    "WHERE id = '" + bankAccount.getLocalID() + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public BankAccount getBankAccount(int userID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_bank WHERE user_id = ?",
                                          new String[]{Integer.toString(userID)});
        return getBankAccountFromCursorWithClose(cursor);
    }

    // Set of Book
    public boolean insertSetOfBook(SetOfBook setOfBook)
    {
        try
        {
            String sqlString = "INSERT INTO tbl_sob (server_id, user_id, name) VALUES (" +
                    "'" + setOfBook.getServerID() + "'," +
                    "'" + setOfBook.getUserID() + "'," +
                    "'" + sqliteEscape(setOfBook.getName()) + "')";
            database.execSQL(sqlString);

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUserSetOfBooks(int userID)
    {
        try
        {
            String sqlString = "DELETE FROM tbl_sob WHERE user_id = '" + userID + "'";
            database.execSQL(sqlString);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserSetOfBooks(List<SetOfBook> setOfBookList, int userID)
    {
        try
        {
            deleteUserSetOfBooks(userID);
            for (SetOfBook setOfBook : setOfBookList)
            {
                insertSetOfBook(setOfBook);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public List<SetOfBook> getUserSetOfBooks(int userID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_sob WHERE user_id = ?",
                                          new String[]{Integer.toString(userID)});
        return getSetOfBookListFromCursorWithClose(cursor);
    }

    public String getUserSetOfBookIDs(int userID)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_sob WHERE user_id = ?",
                                          new String[]{Integer.toString(userID)});
        List<Integer> setOfBookList = new ArrayList<>();
        setOfBookList.add(0);
        try
        {
            while (cursor.moveToNext())
            {
                setOfBookList.add(getIntFromCursor(cursor, "server_id"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return TextUtils.join(",", setOfBookList);
    }

    // Auxiliaries
    private String sqliteEscape(String keyWord)
    {
        keyWord = keyWord.replace("/", "//");
        keyWord = keyWord.replace("'", "''");
        keyWord = keyWord.replace("[", "/[");
        keyWord = keyWord.replace("]", "/]");
        keyWord = keyWord.replace("%", "/%");
        keyWord = keyWord.replace("&", "/&");
        keyWord = keyWord.replace("_", "/_");
        keyWord = keyWord.replace("(", "/(");
        keyWord = keyWord.replace(")", "/)");
        return keyWord;
    }

    private double getDoubleFromCursor(Cursor cursor, String columnName)
    {
        return cursor.getDouble(cursor.getColumnIndex(columnName));
    }

    private int getIntFromCursor(Cursor cursor, String columnName)
    {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    private String getStringFromCursor(Cursor cursor, String columnName)
    {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    private boolean getBooleanFromCursor(Cursor cursor, String columnName)
    {
        return cursor.getInt(cursor.getColumnIndex(columnName)) > 0;
    }

    // Auxiliaries - Group
    private Group getGroupFromCursor(Cursor cursor)
    {
        Group group = new Group();
        group.setServerID(getIntFromCursor(cursor, "server_id"));
        group.setName(getStringFromCursor(cursor, "group_name"));
        group.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
        group.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));

        return group;
    }

    private Group getGroupFromCursorWithClose(Cursor cursor)
    {
        Group group = null;
        try
        {
            if (cursor.moveToNext())
            {
                group = getGroupFromCursor(cursor);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return group;
    }

    // Auxiliaries - User
    private User getUserFromCursor(Cursor cursor)
    {
        User user = new User();
        user.setServerID(getIntFromCursor(cursor, "server_id"));
        user.setEmail(getStringFromCursor(cursor, "email"));
        user.setPhone(getStringFromCursor(cursor, "phone"));
        user.setWeChat(getStringFromCursor(cursor, "wechat"));
        user.setDidi(getStringFromCursor(cursor, "didi"));
        user.setDidiToken(getStringFromCursor(cursor, "didi_token"));
        user.setNickname(getStringFromCursor(cursor, "nickname"));
        user.setAvatarID(getIntFromCursor(cursor, "avatar_id"));
        user.setAvatarServerPath(getStringFromCursor(cursor, "avatar_server_path"));
        user.setAvatarLocalPath(getStringFromCursor(cursor, "avatar_local_path"));
        user.setPrivilege(getIntFromCursor(cursor, "privilege"));
        user.setDefaultManagerID(getIntFromCursor(cursor, "manager_id"));
        user.setGroupID(getIntFromCursor(cursor, "group_id"));
        user.setAppliedCompany(getStringFromCursor(cursor, "applied_company"));
        user.setIsAdmin(getBooleanFromCursor(cursor, "admin"));
        user.setIsActive(getBooleanFromCursor(cursor, "active"));
        user.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
        user.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));

        return user;
    }

    private User getUserFromCursorWithClose(Cursor cursor)
    {
        User user = null;
        try
        {
            if (cursor.moveToNext())
            {
                user = getUserFromCursor(cursor);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return user;
    }

    // Auxiliaries - Item
    private Item getItemFromCursor(Cursor cursor)
    {
        Item item = new Item();
        item.setLocalID(getIntFromCursor(cursor, "id"));
        item.setServerID(getIntFromCursor(cursor, "server_id"));
        item.setVendor(getStringFromCursor(cursor, "vendor"));
        item.setAmount(getDoubleFromCursor(cursor, "amount"));
        item.setAaAmount(getDoubleFromCursor(cursor, "pa_amount"));
        item.setNote(getStringFromCursor(cursor, "note"));
        item.setStatus(getIntFromCursor(cursor, "status"));
        item.setLocation(getStringFromCursor(cursor, "location"));
        item.setCurrency(getCurrency(getStringFromCursor(cursor, "currency")));
        item.setRate(getDoubleFromCursor(cursor, "rate"));
        item.setDidiID(getIntFromCursor(cursor, "didi_id"));
        item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
        item.setCreatedDate(getIntFromCursor(cursor, "createdt"));
        item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
        item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
        item.setType(getIntFromCursor(cursor, "type"));
        item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
        item.setAaApproved(getBooleanFromCursor(cursor, "aa_approved"));
        item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
        item.setBelongReport(getReportByLocalID(getIntFromCursor(cursor, "report_local_id")));
        item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));
        item.setInvoices(getItemImages(item.getLocalID()));
        item.setRelevantUsers(getRelevantUsers(item.getLocalID()));
        item.setTags(getItemTags(item.getLocalID()));

        return item;
    }

    private Item getItemFromCursorWithClose(Cursor cursor)
    {
        Item item = null;
        try
        {
            if (cursor.moveToNext())
            {
                item = getItemFromCursor(cursor);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return item;
    }

    private List<Item> getItemListFromCursorWithClose(Cursor cursor)
    {
        List<Item> itemList = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                itemList.add(getItemFromCursor(cursor));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return itemList;
    }

    private Item getOthersItemFromCursor(Cursor cursor)
    {
        Item item = new Item();
        item.setLocalID(getIntFromCursor(cursor, "id"));
        item.setServerID(getIntFromCursor(cursor, "server_id"));
        item.setVendor(getStringFromCursor(cursor, "vendor"));
        item.setAmount(getDoubleFromCursor(cursor, "amount"));
        item.setAaAmount(getDoubleFromCursor(cursor, "pa_amount"));
        item.setNote(getStringFromCursor(cursor, "note"));
        item.setStatus(getIntFromCursor(cursor, "status"));
        item.setLocation(getStringFromCursor(cursor, "location"));
        item.setCurrency(getCurrency(getStringFromCursor(cursor, "currency")));
        item.setRate(getDoubleFromCursor(cursor, "rate"));
        item.setDidiID(getIntFromCursor(cursor, "didi_id"));
        item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
        item.setCreatedDate(getIntFromCursor(cursor, "createdt"));
        item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
        item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
        item.setType(getIntFromCursor(cursor, "type"));
        item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
        item.setAaApproved(getBooleanFromCursor(cursor, "aa_approved"));
        item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
        item.setBelongReport(getOthersReport(getIntFromCursor(cursor, "report_server_id")));
        item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));
        item.setInvoices(getOthersItemImages(item.getServerID()));
        item.setRelevantUsers(User.idStringToUserList(getStringFromCursor(cursor, "users_id")));
        item.setTags(Tag.idStringToTagList(getStringFromCursor(cursor, "tags_id")));

        return item;
    }

    private Item getOthersItemFromCursorWithClose(Cursor cursor)
    {
        Item item = null;
        try
        {
            if (cursor.moveToNext())
            {
                item = getOthersItemFromCursor(cursor);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return item;
    }

    private List<Item> getOthersItemListFromCursorWithClose(Cursor cursor)
    {
        List<Item> itemList = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                itemList.add(getOthersItemFromCursor(cursor));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return itemList;
    }

    // Auxiliaries - Report
    private Report getReportFromCursor(Cursor cursor)
    {
        Report report = new Report();
        report.setLocalID(getIntFromCursor(cursor, "id"));
        report.setServerID(getIntFromCursor(cursor, "server_id"));
        report.setTitle(getStringFromCursor(cursor, "title"));
        report.setSender(getUser(getIntFromCursor(cursor, "user_id")));
        report.setManagerList(User.idStringToUserList(getStringFromCursor(cursor, "manager_id")));
        report.setCCList(User.idStringToUserList(getStringFromCursor(cursor, "cc_id")));
        report.setCommentList(getReportComments(report.getLocalID()));
        report.setType(getIntFromCursor(cursor, "type"));
        report.setStatus(getIntFromCursor(cursor, "status"));
        report.setAaApproved(getBooleanFromCursor(cursor, "aa_approved"));
        report.setCreatedDate(getIntFromCursor(cursor, "created_date"));
        report.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
        report.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));

        return report;
    }

    private Report getReportFromCursorWithClose(Cursor cursor)
    {
        Report report = null;
        try
        {
            if (cursor.moveToNext())
            {
                report = getReportFromCursor(cursor);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return report;
    }

    private List<Report> getReportListFromCursorWithClose(Cursor cursor)
    {
        List<Report> reportList = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                reportList.add(getReportFromCursor(cursor));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return reportList;
    }

    private Report getOthersReportFromCursor(Cursor cursor)
    {
        Report report = new Report();
        report.setLocalID(getIntFromCursor(cursor, "id"));
        report.setServerID(getIntFromCursor(cursor, "server_id"));
        report.setTitle(getStringFromCursor(cursor, "title"));
        report.setSender(getUser(getIntFromCursor(cursor, "user_id")));
        report.setManagerList(User.idStringToUserList(getStringFromCursor(cursor, "manager_id")));
        report.setCCList(User.idStringToUserList(getStringFromCursor(cursor, "cc_id")));
        report.setCommentList(getOthersReportComments(report.getServerID()));
        report.setStatus(getIntFromCursor(cursor, "status"));
        report.setMyDecision(getIntFromCursor(cursor, "my_decision"));
        report.setType(getIntFromCursor(cursor, "type"));
        report.setItemCount(getIntFromCursor(cursor, "item_count"));
        report.setAmount(getStringFromCursor(cursor, "amount"));
        report.setIsCC(Utils.intToBoolean(getIntFromCursor(cursor, "is_cc")));
        report.setStep(getIntFromCursor(cursor, "step"));
        report.setCreatedDate(getIntFromCursor(cursor, "created_date"));
        report.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
        report.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));

        return report;
    }

    private Report getOthersReportFromCursorWithClose(Cursor cursor)
    {
        Report report = null;
        try
        {
            if (cursor.moveToNext())
            {
                report = getOthersReportFromCursor(cursor);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return report;
    }

    private List<Report> getOthersReportListFromCursorWithClose(Cursor cursor)
    {
        List<Report> reportList = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                reportList.add(getOthersReportFromCursor(cursor));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return reportList;
    }

    // Auxiliaries - Comment
    private Comment getCommentFromCursor(Cursor cursor)
    {
        Comment comment = new Comment();
        comment.setLocalID(getIntFromCursor(cursor, "id"));
        comment.setServerID(getIntFromCursor(cursor, "server_id"));
        comment.setReviewer(getUser(getIntFromCursor(cursor, "user_id")));
        comment.setReportID(getIntFromCursor(cursor, "report_local_id"));
        comment.setContent(getStringFromCursor(cursor, "comment"));
        comment.setCreatedDate(getIntFromCursor(cursor, "comment_date"));
        comment.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
        comment.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));

        return comment;
    }

    private List<Comment> getCommentListFromCursorWithClose(Cursor cursor)
    {
        List<Comment> commentList = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                commentList.add(getCommentFromCursor(cursor));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return commentList;
    }

    private Comment getOthersCommentFromCursor(Cursor cursor)
    {
        Comment comment = new Comment();
        comment.setLocalID(getIntFromCursor(cursor, "id"));
        comment.setServerID(getIntFromCursor(cursor, "server_id"));
        comment.setReviewer(getUser(getIntFromCursor(cursor, "user_id")));
        comment.setReportID(getIntFromCursor(cursor, "report_server_id"));
        comment.setContent(getStringFromCursor(cursor, "comment"));
        comment.setCreatedDate(getIntFromCursor(cursor, "comment_date"));
        comment.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
        comment.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));

        return comment;
    }

    private List<Comment> getOthersCommentListFromCursorWithClose(Cursor cursor)
    {
        List<Comment> commentList = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                commentList.add(getOthersCommentFromCursor(cursor));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return commentList;
    }

    // Auxiliaries - Category
    private Category getCategoryFromCursor(Cursor cursor)
    {
        Category category = new Category();
        category.setServerID(getIntFromCursor(cursor, "server_id"));
        category.setName(getStringFromCursor(cursor, "category_name"));
        category.setLimit(getDoubleFromCursor(cursor, "max_limit"));
        category.setGroupID(getIntFromCursor(cursor, "group_id"));
        category.setParentID(getIntFromCursor(cursor, "parent_id"));
        category.setSetOfBookID(getIntFromCursor(cursor, "sob_id"));
        category.setIconID(getIntFromCursor(cursor, "icon_id"));
        category.setType(getIntFromCursor(cursor, "type"));
        category.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
        category.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));

        return category;
    }

    private Category getCategoryFromCursorWithClose(Cursor cursor)
    {
        Category category = null;
        try
        {
            if (cursor.moveToNext())
            {
                category = getCategoryFromCursor(cursor);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return category;
    }

    private List<Category> getCategoryListFromCursorWithClose(Cursor cursor)
    {
        List<Category> categoryList = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                categoryList.add(getCategoryFromCursor(cursor));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return categoryList;
    }

    // Auxiliaries - Tag
    private Tag getTagFromCursor(Cursor cursor)
    {
        Tag tag = new Tag();
        tag.setServerID(getIntFromCursor(cursor, "server_id"));
        tag.setName(getStringFromCursor(cursor, "tag_name"));
        tag.setGroupID(getIntFromCursor(cursor, "group_id"));
        tag.setIconID(getIntFromCursor(cursor, "icon_id"));
        tag.setIconPath(getStringFromCursor(cursor, "icon_path"));
        tag.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
        tag.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));

        return tag;
    }

    private Tag getTagFromCursorWithClose(Cursor cursor)
    {
        Tag tag = null;
        try
        {
            if (cursor.moveToNext())
            {
                tag = getTagFromCursor(cursor);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return tag;
    }

    private List<Tag> getTagListFromCursorWithClose(Cursor cursor)
    {
        List<Tag> tagList = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                tagList.add(getTagFromCursor(cursor));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return tagList;
    }

    // Auxiliaries - Image
    private Image getImageFromCursor(Cursor cursor)
    {
        Image image = new Image();
        image.setLocalID(getIntFromCursor(cursor, "id"));
        image.setServerID(getIntFromCursor(cursor, "server_id"));
        image.setServerPath(getStringFromCursor(cursor, "server_path"));
        image.setLocalPath(getStringFromCursor(cursor, "local_path"));
        image.setItemID(getIntFromCursor(cursor, "item_local_id"));

        return image;
    }

    private Image getImageFromCursorWithClose(Cursor cursor)
    {
        Image image = null;
        try
        {
            if (cursor.moveToNext())
            {
                image = getImageFromCursor(cursor);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return image;
    }

    private List<Image> getImageListFromCursorWithClose(Cursor cursor)
    {
        List<Image> imageList = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                imageList.add(getImageFromCursor(cursor));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return imageList;
    }

    private Image getOthersImageFromCursor(Cursor cursor)
    {
        Image image = new Image();
        image.setLocalID(getIntFromCursor(cursor, "id"));
        image.setServerID(getIntFromCursor(cursor, "server_id"));
        image.setServerPath(getStringFromCursor(cursor, "server_path"));
        image.setLocalPath(getStringFromCursor(cursor, "local_path"));
        image.setItemID(getIntFromCursor(cursor, "item_server_id"));

        return image;
    }

    private Image getOthersImageFromCursorWithClose(Cursor cursor)
    {
        Image image = null;
        try
        {
            if (cursor.moveToNext())
            {
                image = getOthersImageFromCursor(cursor);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return image;
    }

    private List<Image> getOthersImageListFromCursorWithClose(Cursor cursor)
    {
        List<Image> imageList = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                imageList.add(getOthersImageFromCursor(cursor));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return imageList;
    }

    // Auxiliaries - Currency
    private Currency getCurrencyFromCursor(Cursor cursor)
    {
        Currency currency = new Currency();
        currency.setCode(getStringFromCursor(cursor, "code"));
        currency.setSymbol(getStringFromCursor(cursor, "symbol"));
        currency.setRate(getDoubleFromCursor(cursor, "rate"));

        return currency;
    }

    private Currency getCurrencyFromCursorWithClose(Cursor cursor)
    {
        Currency currency = null;
        try
        {
            if (cursor.moveToNext())
            {
                currency = getCurrencyFromCursor(cursor);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return currency;
    }

    private List<Currency> getCurrencyListFromCursorWithClose(Cursor cursor)
    {
        List<Currency> currencyList = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                currencyList.add(getCurrencyFromCursor(cursor));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return currencyList;
    }

    // Auxiliaries - Bank Account
    private BankAccount getBankAccountFromCursor(Cursor cursor)
    {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setLocalID(getIntFromCursor(cursor, "id"));
        bankAccount.setServerID(getIntFromCursor(cursor, "server_id"));
        bankAccount.setName(getStringFromCursor(cursor, "name"));
        bankAccount.setNumber(getStringFromCursor(cursor, "number"));
        bankAccount.setBankName(getStringFromCursor(cursor, "bank_name"));
        bankAccount.setLocation(getStringFromCursor(cursor, "location"));

        return bankAccount;
    }

    private BankAccount getBankAccountFromCursorWithClose(Cursor cursor)
    {
        BankAccount bankAccount = null;
        try
        {
            if (cursor.moveToNext())
            {
                bankAccount = getBankAccountFromCursor(cursor);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return bankAccount;
    }

    // Auxiliaries - Set of Book
    private SetOfBook getSetOfBookFromCursor(Cursor cursor)
    {
        SetOfBook setOfBook = new SetOfBook();
        setOfBook.setServerID(getIntFromCursor(cursor, "server_id"));
        setOfBook.setUserID(getIntFromCursor(cursor, "user_id"));
        setOfBook.setName(getStringFromCursor(cursor, "name"));

        return setOfBook;
    }

    private List<SetOfBook> getSetOfBookListFromCursorWithClose(Cursor cursor)
    {
        List<SetOfBook> setOfBookList = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                setOfBookList.add(getSetOfBookFromCursor(cursor));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return setOfBookList;
    }
}
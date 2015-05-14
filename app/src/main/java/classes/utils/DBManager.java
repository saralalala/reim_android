package classes.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import classes.model.Category;
import classes.model.Comment;
import classes.model.Group;
import classes.model.Image;
import classes.model.Item;
import classes.model.Report;
import classes.model.Tag;
import classes.model.User;

public class DBManager extends SQLiteOpenHelper
{
	private static DBManager dbManager = null;
	private static SQLiteDatabase database = null;
	
	private static final String DATABASE_NAME = "reim.db";
	private static final int DATABASE_VERSION = 1;
	
	private DBManager(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db)
	{
		try
		{
			String createGroupTable="CREATE TABLE IF NOT EXISTS tbl_group ("
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

			String createUserTable="CREATE TABLE IF NOT EXISTS tbl_user ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "server_id INT DEFAULT(0),"
										+ "email TEXT DEFAULT(''),"
										+ "phone TEXT DEFAULT(''),"
										+ "nickname TEXT DEFAULT(''),"
                                        + "bank_account TEXT DEFAULT(''),"
										+ "avatar_id INT DEFAULT(0),"
                                        + "avatar_server_path TEXT DEFAULT(''),"
										+ "avatar_local_path TEXT DEFAULT(''),"
										+ "privilege INT DEFAULT(0),"
										+ "manager_id INT DEFAULT(0),"
										+ "group_id INT DEFAULT(0),"
                                        + "applied_company TEXT DEFAULT(''),"
										+ "admin INT DEFAULT(0),"
										+ "server_updatedt INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
			db.execSQL(createUserTable);
			
			String createItemTable="CREATE TABLE IF NOT EXISTS tbl_item ("
									+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
									+ "server_id INT DEFAULT(0),"
                                    + "type INT DEFAULT(0),"
									+ "vendor TEXT DEFAULT(''),"
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

			String createItemUserTable="CREATE TABLE IF NOT EXISTS tbl_item_user ("
											+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
											+ "item_local_id INT DEFAULT(0),"
											+ "user_id INT DEFAULT(0),"
											+ "local_updatedt INT DEFAULT(0),"
											+ "backup1 INT DEFAULT(0),"
											+ "backup2 TEXT DEFAULT(''),"
											+ "backup3 TEXT DEFAULT('')"
											+ ")";
			db.execSQL(createItemUserTable);

			String createItemTagTable="CREATE TABLE IF NOT EXISTS tbl_item_tag ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "item_local_id INT DEFAULT(0),"
										+ "tag_id INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
			db.execSQL(createItemTagTable);
			
			String createOthersItemTable="CREATE TABLE IF NOT EXISTS tbl_others_item ("
									+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
									+ "server_id INT DEFAULT(0),"
                                    + "type INT DEFAULT(0),"
									+ "vendor TEXT DEFAULT(''),"
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
			
			String createReportTable="CREATE TABLE IF NOT EXISTS tbl_report ("
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
			
			String createOthersReportTable="CREATE TABLE IF NOT EXISTS tbl_others_report ("
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
										+ "created_date INT DEFAULT(0),"
										+ "server_updatedt INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
			db.execSQL(createOthersReportTable);

			String createCommentTable="CREATE TABLE IF NOT EXISTS tbl_comment ("
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

			String createOthersCommentTable="CREATE TABLE IF NOT EXISTS tbl_others_comment ("
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

			String createCategoryTable="CREATE TABLE IF NOT EXISTS tbl_category ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "server_id INT DEFAULT(0),"
										+ "category_name TEXT DEFAULT(''),"
										+ "max_limit INT DEFAULT(0),"
										+ "group_id INT DEFAULT(0),"
										+ "parent_id INT DEFAULT(0),"
										+ "icon_id INT DEFAULT(0),"
										+ "type INT DEFAULT(0),"
										+ "server_updatedt INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
			db.execSQL(createCategoryTable);

			String createTagTable="CREATE TABLE IF NOT EXISTS tbl_tag ("
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

			String createImageTable="CREATE TABLE IF NOT EXISTS tbl_image ("
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

			String createOthersImageTable="CREATE TABLE IF NOT EXISTS tbl_others_image ("
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
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w("TaskDBAdapter", "Upgrading from version " + oldVersion + " to " + newVersion);

        if (newVersion > oldVersion)
        {
//            if (oldVersion < 2)
//            {
//                String command = "ALTER TABLE tbl_user ADD COLUMN bank_account TEXT DEFAULT('')";
//                db.execSQL(command);
//            }
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

	public void executeExtraCommand()
	{
//		String sqlString = "DELETE FROM tbl_report WHERE id = 23";
//		database.execSQL(sqlString);
//		String sqlString = "DROP TABLE IF EXISTS tbl_category";
//		database.execSQL(sqlString);		
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
	
	public Group getGroup(int groupServerID)
	{
		try
		{	
			Cursor cursor = database.rawQuery("SELECT server_id, group_name, local_updatedt, server_updatedt" +
											  " FROM tbl_group WHERE server_id = ?", new String[]{Integer.toString(groupServerID)});
			if (cursor.moveToNext())
			{
				Group group = new Group();
				group.setServerID(getIntFromCursor(cursor, "server_id"));
				group.setName(getStringFromCursor(cursor, "group_name"));
				group.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				group.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));

				cursor.close();
				return group;
			}
			else
			{
				cursor.close();
				return null;				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	// User
	public boolean insertUser(User user)
	{
		try
		{
			String sqlString = "INSERT INTO tbl_user (server_id, email, phone, nickname, bank_account, avatar_id, avatar_server_path, avatar_local_path, " +
								"privilege, manager_id, group_id, applied_company, admin, local_updatedt, server_updatedt) VALUES (" +
								"'" + user.getServerID() + "'," +
								"'" + user.getEmail() + "'," +
								"'" + user.getPhone() + "'," +
								"'" + sqliteEscape(user.getNickname()) + "'," +
                                "'" + user.getBankAccount() + "'," +
								"'" + user.getAvatarID() + "'," +
                                "'" + user.getAvatarServerPath() + "'," +
								"'" + user.getAvatarLocalPath() + "'," +
								"'" + user.getPrivilege() + "'," +
								"'" + user.getDefaultManagerID() + "'," +
								"'" + user.getGroupID() + "'," +
                                "'" + user.getAppliedCompany() + "'," +
								"'" + Utils.booleanToInt(user.isAdmin()) + "'," +
								"'" + user.getLocalUpdatedDate() + "'," +
								"'" + user.getServerUpdatedDate() + "')";			
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
								"nickname = '" + sqliteEscape(user.getNickname()) + "'," +
                                "bank_account = '" + user.getBankAccount() + "'," +
								"avatar_id = '" + user.getAvatarID() + "'," +
                                "avatar_server_path = '" + user.getAvatarServerPath() + "'," +
								"avatar_local_path = '" + user.getAvatarLocalPath() + "'," +
								"manager_id = '" + user.getDefaultManagerID() + "'," +
								"group_id = '" + user.getGroupID() + "'," +
                                "applied_company = '" + user.getAppliedCompany() + "'," +
								"admin = '" + Utils.booleanToInt(user.isAdmin()) + "'," +
								"local_updatedt = '" + user.getLocalUpdatedDate() + "'," +
								"server_updatedt = '" + user.getServerUpdatedDate() + "' " +
								"WHERE server_id = '" + user.getServerID() + "'";			
			database.execSQL(sqlString);
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
	
	public User getUser(int userServerID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT server_id, email, phone, nickname, bank_account, avatar_id, avatar_server_path, " +
											  "avatar_local_path, privilege, manager_id, group_id, applied_company, admin, local_updatedt, " +
					                          "server_updatedt FROM tbl_user WHERE server_id = ?", new String[]{Integer.toString(userServerID)});
			if (cursor.moveToNext())
			{
				User user = new User();
				user.setServerID(getIntFromCursor(cursor, "server_id"));
				user.setEmail(getStringFromCursor(cursor, "email"));
				user.setPhone(getStringFromCursor(cursor, "phone"));
				user.setNickname(getStringFromCursor(cursor, "nickname"));
                user.setBankAccount(getStringFromCursor(cursor, "bank_account"));
				user.setAvatarID(getIntFromCursor(cursor, "avatar_id"));
                user.setAvatarServerPath(getStringFromCursor(cursor, "avatar_server_path"));
				user.setAvatarLocalPath(getStringFromCursor(cursor, "avatar_local_path"));
				user.setPrivilege(getIntFromCursor(cursor, "privilege"));
				user.setDefaultManagerID(getIntFromCursor(cursor, "manager_id"));
				user.setGroupID(getIntFromCursor(cursor, "group_id"));
                user.setAppliedCompany(getStringFromCursor(cursor, "applied_company"));
				user.setIsAdmin(getBooleanFromCursor(cursor, "admin"));
				user.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				user.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				
				cursor.close();
				return user;
			}
			else
			{
				cursor.close();
				return null;				
			}
		}
		catch (Exception e)
		{
			Log.i("reim", e.toString());
			return null;
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
					if (localUser.getServerID() == user.getServerID() && localUser.getAvatarID() == user.getAvatarID())
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
		List<User> userList = new ArrayList<User>();
		try
        {
            if (groupServerID != -1 && groupServerID != 0)
            {
                Cursor cursor = database.rawQuery("SELECT server_id, email, phone, nickname, bank_account, avatar_id, avatar_server_path, avatar_local_path, " +
                                                          "privilege, manager_id, group_id, applied_company, admin, local_updatedt, server_updatedt " +
                                                          "FROM tbl_user WHERE group_id = ?", new String[]{Integer.toString(groupServerID)});
                while (cursor.moveToNext())
                {
                    User user = new User();
                    user.setServerID(getIntFromCursor(cursor, "server_id"));
                    user.setEmail(getStringFromCursor(cursor, "email"));
                    user.setPhone(getStringFromCursor(cursor, "phone"));
                    user.setNickname(getStringFromCursor(cursor, "nickname"));
                    user.setBankAccount(getStringFromCursor(cursor, "bank_account"));
                    user.setAvatarID(getIntFromCursor(cursor, "avatar_id"));
                    user.setAvatarServerPath(getStringFromCursor(cursor, "avatar_server_path"));
                    user.setAvatarLocalPath(getStringFromCursor(cursor, "avatar_local_path"));
                    user.setPrivilege(getIntFromCursor(cursor, "privilege"));
                    user.setDefaultManagerID(getIntFromCursor(cursor, "manager_id"));
                    user.setGroupID(getIntFromCursor(cursor, "group_id"));
                    user.setAppliedCompany(getStringFromCursor(cursor, "applied_company"));
                    user.setIsAdmin(getBooleanFromCursor(cursor, "admin"));
                    user.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
                    user.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
                    userList.add(user);
                }

                cursor.close();
            }
            else
            {
                User user = AppPreference.getAppPreference().getCurrentUser();
                if (user != null)
                {
                    userList.add(user);
                }
            }
			return userList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return userList;
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
		List<User> relevantUsers = new ArrayList<User>();	
		try
		{		
			Cursor userCursor = database.rawQuery("SELECT user_id FROM tbl_item_user WHERE item_local_id = ?", 
													new String[]{Integer.toString(itemLocalID)});
			while (userCursor.moveToNext())
			{
				User user = getUser(getIntFromCursor(userCursor, "user_id"));
				if (user != null)
				{
					relevantUsers.add(user);
				}
			}

			return relevantUsers;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return relevantUsers;
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
	
	// Item
	public int insertItem(Item item)
	{
		try
		{
			System.out.println("insert item: local id = " + item.getLocalID() + ", server id = " + item.getServerID());
			int reportID = item.getBelongReport() == null? -1 : item.getBelongReport().getLocalID();
			int categoryID = item.getCategory() == null? -1 : item.getCategory().getServerID();
			String sqlString = "INSERT INTO tbl_item (server_id, vendor, report_local_id, category_id, amount, pa_amount, " +
							   							"user_id, consumed_date, note, status, location, createdt, server_updatedt, " +
							   							"local_updatedt, type, need_reimbursed, aa_approved) VALUES (" +
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
			int categoryID = item.getCategory() == null? -1 : item.getCategory().getServerID();
			String sqlString = "INSERT INTO tbl_others_item (server_id, vendor, report_server_id, category_id, tags_id, users_id, " +
							   							"amount, pa_amount, user_id, consumed_date, note, status, location, createdt, " +
							   							"server_updatedt, local_updatedt, type, need_reimbursed, aa_approved) VALUES (" +
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
			System.out.println("update item by local id: local id = " + item.getLocalID() + ", server id = " + item.getServerID());
			int reportID = item.getBelongReport() == null? -1 : item.getBelongReport().getLocalID();
			int categoryID = item.getCategory() == null? -1 : item.getCategory().getServerID();
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
			System.out.println("update item by server id: local id = " + item.getLocalID() + ", server id = " + item.getServerID());
			int reportID = item.getBelongReport() == null? -1 : item.getBelongReport().getLocalID();
			int categoryID = item.getCategory() == null? -1 : item.getCategory().getServerID();
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

	public boolean deleteItem(int itemLocalID)
	{
		try
		{
			String sqlString = "DELETE FROM tbl_item WHERE id = '" + itemLocalID +"'";
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
			String idString = !remainingList.isEmpty()? TextUtils.join(",", remainingList) + ", -1" : "-1";
			List<Integer> itemIDList = new ArrayList<Integer>();

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
			else if (item.getLocalID() == -1 && item.getServerID() != -1)
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
	
	public Item getItemByLocalID(int itemLocalID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE id = ?", new String[]{Integer.toString(itemLocalID)});

			if (cursor.moveToNext())
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
				item.setInvoices(getItemImages(itemLocalID));
				item.setRelevantUsers(getRelevantUsers(itemLocalID));
				item.setTags(getItemTags(itemLocalID));

				cursor.close();
				return item;
			}
			else
			{
				cursor.close();
				return null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public Item getItemByServerID(int itemServerID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE server_id = ?", new String[]{Integer.toString(itemServerID)});

			if (cursor.moveToNext())
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

				cursor.close();
				return item;
			}
			else
			{
				cursor.close();
				return null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public Item getOthersItem(int itemServerID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_item WHERE server_id = ?", 
					new String[]{Integer.toString(itemServerID)});

			if (cursor.moveToNext())
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
				item.setInvoices(getOthersItemImages(itemServerID));
				item.setRelevantUsers(User.idStringToUserList(getStringFromCursor(cursor, "users_id")));
				item.setTags(Tag.idStringToTagList(getStringFromCursor(cursor, "tags_id")));

				cursor.close();
				return item;
			}
			else
			{
				cursor.close();
				return null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Item> getUserItems(int userServerID)
	{
		List<Item> itemList = new ArrayList<Item>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE user_id = ?", new String[]{Integer.toString(userServerID)});

			while (cursor.moveToNext())
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
				
				itemList.add(item);
			}

			cursor.close();
			return itemList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return itemList;
		}
	}
	
	public List<Item> getUnarchivedConsumedItems(int userServerID)
	{
		List<Item> itemList = new ArrayList<Item>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE user_id = ? AND " +
												"(report_local_id = -1 OR report_local_id = 0) AND " +
												"(type = 0 OR (type = 1 AND aa_approved = 1) OR (type = 2 AND aa_approved = 1))",
													new String[]{Integer.toString(userServerID)});

			while (cursor.moveToNext())
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
				
				itemList.add(item);
			}

			cursor.close();
			return itemList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return itemList;
		}
	}
	
	public List<Item> getUnarchivedBudgetItems(int userServerID)
	{
		List<Item> itemList = new ArrayList<Item>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE user_id = ? AND " +
												"(report_local_id = -1 OR report_local_id = 0) AND " +
												"type = 1 AND aa_approved = 0",
													new String[]{Integer.toString(userServerID)});

			while (cursor.moveToNext())
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
				
				itemList.add(item);
			}

			cursor.close();
			return itemList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return itemList;
		}
	}

    public List<Item> getUnarchivedBorrowingItems(int userServerID)
    {
        List<Item> itemList = new ArrayList<Item>();
        try
        {
            Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE user_id = ? AND " +
                                                      "(report_local_id = -1 OR report_local_id = 0) AND " +
                                                      "type = 2 AND aa_approved = 0",
                                              new String[]{Integer.toString(userServerID)});

            while (cursor.moveToNext())
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

                itemList.add(item);
            }

            cursor.close();
            return itemList;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return itemList;
        }
    }

	public List<Item> getUnsyncedItems(int userServerID)
	{
		List<Item> itemList = new ArrayList<Item>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE local_updatedt > server_updatedt AND user_id = ?", 
													new String[]{Integer.toString(userServerID)});

			while (cursor.moveToNext())
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
				
				itemList.add(item);
			}

			cursor.close();
			return itemList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return itemList;
		}
	}
	
	public List<Item> getExistsUserItems(int userServerID)
	{
		List<Item> itemList = new ArrayList<Item>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT id, server_id FROM tbl_item WHERE user_id = ? AND server_id != -1", 
												new String[]{Integer.toString(userServerID)});

			while (cursor.moveToNext())
			{
				Item item = new Item();
				item.setLocalID(getIntFromCursor(cursor, "id"));
				item.setServerID(getIntFromCursor(cursor, "server_id"));
				itemList.add(item);
			}

			cursor.close();
			return itemList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return itemList;
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
		List<Item> itemList = new ArrayList<Item>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE report_local_id = ?", 
													new String[]{Integer.toString(reportLocalID)});

			while (cursor.moveToNext())
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
				
				itemList.add(item);
			}

			cursor.close();
			return itemList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return itemList;	
		}
	}
	
	public List<Item> getOthersReportItems(int reportServerID)
	{
		List<Item> itemList = new ArrayList<Item>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_item WHERE report_server_id = ?", 
													new String[]{Integer.toString(reportServerID)});
			
			while (cursor.moveToNext())
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
				item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
				item.setCreatedDate(getIntFromCursor(cursor, "createdt"));
				item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				item.setType(getIntFromCursor(cursor, "type"));
				item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
				item.setAaApproved(getBooleanFromCursor(cursor, "aa_approved"));
				item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
				item.setBelongReport(getOthersReport(reportServerID));
				item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));
				item.setInvoices(getOthersItemImages(item.getServerID()));
				item.setRelevantUsers(User.idStringToUserList(getStringFromCursor(cursor, "users_id")));
				item.setTags(Tag.idStringToTagList(getStringFromCursor(cursor, "tags_id")));
				
				itemList.add(item);
			}

			cursor.close();
			return itemList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return itemList;	
		}
	}
	
	public List<Item> getItems(ArrayList<Integer> chosenItemIDList)
	{
		List<Item> itemList = new ArrayList<Item>();
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

	// Report
	public int insertReport(Report report)
	{
		try
		{
            System.out.println("insert report: local id = " + report.getLocalID() + ", server id = " + report.getServerID());
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
									"type, amount, item_count, is_cc, created_date, server_updatedt, local_updatedt) VALUES (" +
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
	
	public boolean updateReportByLocalID(Report report)
	{
		try
		{
            System.out.println("update report by local id: local id = " + report.getLocalID() + ", server id = " + report.getServerID());
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
            System.out.println("update report by server id: local id = " + report.getLocalID() + ", server id = " + report.getServerID());
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
            String amountString = report.getAmount() == null? "" : "amount = '" + report.getAmount() + "',";
			String sqlString = "UPDATE tbl_others_report SET " + amountString +
								"server_id = '" + report.getServerID() + "'," +
								"title = '" + sqliteEscape(report.getTitle()) + "'," +
                                "owner_id = '" + AppPreference.getAppPreference().getCurrentUserID() + "'," +
								"user_id = '" + report.getSender().getServerID() + "'," +
								"status = '" + report.getStatus() + "'," +
								"my_decision = '" + report.getMyDecision() + "'," +
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

	public boolean deleteReport(int reportLocalID)
	{
		try
		{
			String sqlString = "DELETE FROM tbl_report WHERE id = '" + reportLocalID +"'";
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
			String sqlString = "DELETE FROM tbl_others_report WHERE server_id = '" + reportServerID +"' AND owner_id = '" + managerID + "'";
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
			String sqlString = "DELETE FROM tbl_others_report WHERE owner_id = '" + managerID +"'";
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
			String idString = !remainingList.isEmpty()? TextUtils.join(",", remainingList) + ",-1" : "-1";
			List<Integer> reportIDList = new ArrayList<Integer>();

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
	
	public Report getReportByLocalID(int reportLocalID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_report WHERE id = ? ", 
												new String[]{Integer.toString(reportLocalID)});
			
			if (cursor.moveToNext())
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

				cursor.close();
				return report;
			}
			else
			{
				cursor.close();
				return null;
			}
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public Report getReportByServerID(int reportServerID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_report WHERE server_id = ? ", 
													new String[]{Integer.toString(reportServerID)});
			
			if (cursor.moveToNext())
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

				cursor.close();
				return report;
			}
			else
			{
				cursor.close();
				return null;
			}
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public Report getOthersReport(int reportServerID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_report WHERE server_id = ?", 
											new String[]{Integer.toString(reportServerID)});
			
			if (cursor.moveToNext())
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
				report.setCreatedDate(getIntFromCursor(cursor, "created_date"));
				report.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				report.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));

				cursor.close();
				return report;
			}
			else
			{
				cursor.close();
				return null;
			}
		}
		catch (Exception e)
		{
			return null;
		}
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
			else if (report.getLocalID() == -1 && report.getServerID() != -1)
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
	
	public List<Report> getUnsyncedUserReports(int userServerID)
	{
		List<Report> reportList = new ArrayList<Report>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_report WHERE local_updatedt > server_updatedt AND " +
												"(user_id = ? OR manager_id = ?)", 
												new String[]{Integer.toString(userServerID), Integer.toString(userServerID)});
			
			while (cursor.moveToNext())
			{
				Report report = new Report();
				report.setLocalID(getIntFromCursor(cursor, "id"));
				report.setServerID(getIntFromCursor(cursor, "server_id"));
				report.setTitle(getStringFromCursor(cursor, "title"));
				report.setSender(getUser(getIntFromCursor(cursor, "user_id")));
				report.setManagerList(User.idStringToUserList(getStringFromCursor(cursor, "manager_id")));
				report.setCCList(User.idStringToUserList(getStringFromCursor(cursor, "cc_id")));
				report.setStatus(getIntFromCursor(cursor, "status"));
				report.setType(getIntFromCursor(cursor, "type"));
                report.setAaApproved(getBooleanFromCursor(cursor, "aa_approved"));
				report.setCreatedDate(getIntFromCursor(cursor, "created_date"));
				report.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				report.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				
				reportList.add(report);
			}
			
			cursor.close();
			return reportList;
		}
		catch (Exception e)
		{
			return reportList;
		}
	}
	
	public List<Report> getExistsUserReports(int userServerID)
	{
		List<Report> reportList = new ArrayList<Report>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT id, server_id FROM tbl_report WHERE user_id = ? AND server_id != -1", 
														new String[]{Integer.toString(userServerID)});

			while (cursor.moveToNext())
			{
				Report report = new Report();
				report.setLocalID(getIntFromCursor(cursor, "id"));
				report.setServerID(getIntFromCursor(cursor, "server_id"));
				
				reportList.add(report);
			}

			cursor.close();
			return reportList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return reportList;
		}	
	}
	
	public List<Report> getUserReports(int userServerID)
	{
		List<Report> reportList = new ArrayList<Report>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_report WHERE user_id = ?", 
											new String[]{Integer.toString(userServerID)});
			
			while (cursor.moveToNext())
			{
				Report report = new Report();
				report.setLocalID(getIntFromCursor(cursor, "id"));
				report.setServerID(getIntFromCursor(cursor, "server_id"));
				report.setTitle(getStringFromCursor(cursor, "title"));
				report.setSender(getUser(getIntFromCursor(cursor, "user_id")));
				report.setManagerList(User.idStringToUserList(getStringFromCursor(cursor, "manager_id")));
				report.setCCList(User.idStringToUserList(getStringFromCursor(cursor, "cc_id")));
				report.setCommentList(getReportComments(report.getLocalID()));
				report.setStatus(getIntFromCursor(cursor, "status"));
				report.setType(getIntFromCursor(cursor, "type"));
                report.setAaApproved(getBooleanFromCursor(cursor, "aa_approved"));
				report.setCreatedDate(getIntFromCursor(cursor, "created_date"));
				report.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				report.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));

				reportList.add(report);
			}
			
			cursor.close();
			return reportList;
		}
		catch (Exception e)
		{
			return reportList;
		}
	}
	
	public List<Report> getOthersReports(int userServerID)
	{
		List<Report> reportList = new ArrayList<Report>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_report WHERE owner_id = ?", 
											new String[]{Integer.toString(userServerID)});
			
			while (cursor.moveToNext())
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
				report.setCreatedDate(getIntFromCursor(cursor, "created_date"));
				report.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				report.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				
				reportList.add(report);
			}
			
			cursor.close();
			return reportList;
		}
		catch (Exception e)
		{
			return reportList;
		}
	}

	public String getReportItemIDs(int reportLocalID)
	{
		List<Integer> idList = new ArrayList<Integer>();
		Cursor cursor = database.rawQuery("SELECT server_id FROM tbl_item WHERE report_local_id = ?", 
												new String[]{Integer.toString(reportLocalID)});
		while (cursor.moveToNext())
		{
			idList.add(getIntFromCursor(cursor, "server_id"));
		}

		cursor.close();
		return TextUtils.join(",", idList);
	}

	public double getReportAmount(int reportLocalID)
	{
		double amount = 0;
		Cursor cursor = null;

        try
        {
            cursor = database.rawQuery("SELECT amount FROM tbl_item WHERE report_local_id = ?",
                              new String[]{Integer.toString(reportLocalID)});

            while (cursor.moveToNext())
            {
                amount += getDoubleFromCursor(cursor, "amount");
            }
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
            return amount;
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
	
	public boolean updateComment(Comment comment)
	{
		try
		{
			String sqlString = "UPDATE tbl_comment SET " + 
								"server_id = '" + comment.getServerID() + "'," +
								"report_local_id = '" + comment.getReportID() + "'," +
								"user_id = '" + comment.getReviewer().getServerID() + "'," +
								"comment = '" + sqliteEscape(comment.getContent()) + "'," +
								"comment_date = '" + comment.getCreatedDate() + "'," +
								"server_updatedt = '" + comment.getServerUpdatedDate() + "'," +
								"local_updatedt = '" + comment.getLocalUpdatedDate() + "' " +
								"WHERE server_id = '" + comment.getLocalID() + "'";		
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
			String sqlString = "DELETE FROM tbl_comment WHERE id = '" + commentLocalID +"'";
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
			String sqlString = "DELETE FROM tbl_others_comment WHERE server_id = '" + commentServerID +"'";
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
			String sqlString = "DELETE FROM tbl_comment WHERE report_local_id = '" + reportLocalID +"'";
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
			String sqlString = "DELETE FROM tbl_others_comment WHERE report_server_id = '" + reportServerID +"'";
			database.execSQL(sqlString);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public Comment getComment(int commentLocalID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_comment WHERE id = ?", 
											new String[]{Integer.toString(commentLocalID)});

			if (cursor.moveToNext())
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

				cursor.close();
				return comment;
			}
			else
			{
				cursor.close();
				return null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Comment> getReportComments(int reportLocalID)
	{
		List<Comment> commentList = new ArrayList<Comment>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_comment WHERE report_local_id = ?", 
														new String[]{Integer.toString(reportLocalID)});

			while (cursor.moveToNext())
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
				
				commentList.add(comment);
			}
			
			cursor.close();
			return commentList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return commentList;
		}		
	}
	
	public List<Comment> getOthersReportComments(int reportServerID)
	{
		List<Comment> commentList = new ArrayList<Comment>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_comment WHERE report_server_id = ?", 
														new String[]{Integer.toString(reportServerID)});

			while (cursor.moveToNext())
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
				
				commentList.add(comment);
			}
			
			cursor.close();
			return commentList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return commentList;
		}		
	}

	// Category
	public boolean insertCategory(Category category)
	{
		try
		{
			String sqlString = "INSERT INTO tbl_category (server_id, category_name, max_limit, group_id, " +
								"parent_id, icon_id, type, local_updatedt, server_updatedt) VALUES (" +
								"'" + category.getServerID() + "'," +
								"'" + sqliteEscape(category.getName()) + "'," +
								"'" + category.getLimit() + "'," +
								"'" + category.getGroupID() + "'," +
								"'" + category.getParentID() + "'," +
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
	
	public boolean updateCategory(Category category)
	{
		try
		{
			String sqlString = "UPDATE tbl_category SET " +
								"category_name = '" + sqliteEscape(category.getName()) + "'," +
								"max_limit = '" + category.getLimit() + "'," +
								"group_id = '" + category.getGroupID() + "'," +
								"parent_id = '" + category.getParentID() + "'," +
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

	public Category getCategory(int categoryServerID)
	{
		try
		{	
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_category WHERE server_id = ?", 
					                          new String[]{Integer.toString(categoryServerID)});
			if (cursor.moveToNext())
			{
				Category category = new Category();
				category.setServerID(getIntFromCursor(cursor, "server_id"));
				category.setName(getStringFromCursor(cursor, "category_name"));
				category.setLimit(getDoubleFromCursor(cursor, "max_limit"));
				category.setGroupID(getIntFromCursor(cursor, "group_id"));
				category.setParentID(getIntFromCursor(cursor, "parent_id"));
				category.setIconID(getIntFromCursor(cursor, "icon_id"));
                category.setType(getIntFromCursor(cursor, "type"));
				category.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				category.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				
				cursor.close();
				return category;
			}
			else
			{
				cursor.close();
				return null;				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
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
		List<Category> categoryList = new ArrayList<Category>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_category WHERE group_id = ? AND parent_id = 0", 
					                          new String[]{Integer.toString(groupServerID)});
			while (cursor.moveToNext())
			{
				Category category = new Category();
				category.setServerID(getIntFromCursor(cursor, "server_id"));
				category.setName(getStringFromCursor(cursor, "category_name"));
				category.setLimit(getDoubleFromCursor(cursor, "max_limit"));
				category.setGroupID(getIntFromCursor(cursor, "group_id"));
				category.setParentID(getIntFromCursor(cursor, "parent_id"));
				category.setIconID(getIntFromCursor(cursor, "icon_id"));
				category.setType(getIntFromCursor(cursor, "type"));
				category.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				category.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				categoryList.add(category);
			}

			cursor.close();
			return categoryList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return categoryList;
		}
	}
	
	public List<Category> getSubCategories(int parentServerID, int groupServerID)
	{
		List<Category> categoryList = new ArrayList<Category>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_category WHERE group_id = ? AND parent_id = ?", 
					                          new String[]{Integer.toString(groupServerID), Integer.toString(parentServerID)});
			while (cursor.moveToNext())
			{
				Category category = new Category();
				category.setServerID(getIntFromCursor(cursor, "server_id"));
				category.setName(getStringFromCursor(cursor, "category_name"));
				category.setLimit(getDoubleFromCursor(cursor, "max_limit"));
				category.setGroupID(getIntFromCursor(cursor, "group_id"));
				category.setParentID(getIntFromCursor(cursor, "parent_id"));
				category.setIconID(getIntFromCursor(cursor, "icon_id"));
				category.setType(getIntFromCursor(cursor, "type"));
				category.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				category.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				categoryList.add(category);
			}
			
			cursor.close();
			return categoryList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return categoryList;
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
	
	public Tag getTag(int tagServerID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_tag WHERE server_id = ?", new String[]{Integer.toString(tagServerID)});
			if (cursor.moveToNext())
			{
				Tag tag = new Tag();
				tag.setServerID(getIntFromCursor(cursor, "server_id"));
				tag.setName(getStringFromCursor(cursor, "tag_name"));
				tag.setGroupID(getIntFromCursor(cursor, "group_id"));
				tag.setIconID(getIntFromCursor(cursor, "icon_id"));
				tag.setIconPath(getStringFromCursor(cursor, "icon_path"));
				tag.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				tag.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				
				cursor.close();
				return tag;
			}
			else
			{
				cursor.close();
				return null;				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
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
		List<Tag> tagList = new ArrayList<Tag>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_tag WHERE group_id = ?", new String[]{Integer.toString(groupServerID)});
			
			while (cursor.moveToNext())
			{
				Tag tag = new Tag();
				tag.setServerID(getIntFromCursor(cursor, "server_id"));
				tag.setName(getStringFromCursor(cursor, "tag_name"));
				tag.setGroupID(getIntFromCursor(cursor, "group_id"));
				tag.setIconID(getIntFromCursor(cursor, "icon_id"));
				tag.setIconPath(getStringFromCursor(cursor, "icon_path"));
				tag.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				tag.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				tagList.add(tag);
			}

			cursor.close();
			return tagList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return tagList;
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
		try
		{
			List<Tag> tags = new ArrayList<Tag>();			
			Cursor tagCursor = database.rawQuery("SELECT tag_id FROM tbl_item_tag WHERE item_local_id = ?", 
													new String[]{Integer.toString(itemLocalID)});
			while (tagCursor.moveToNext())
			{
				Tag tag = getTag(getIntFromCursor(tagCursor, "tag_id"));
				if (tag != null)
				{
					tags.add(tag);
				}
			}
			
			return !tags.isEmpty()? tags : null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
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
	
	public boolean deleteImage(int imageLocalID)
	{
		try
		{
			String sqlString = "DELETE FROM tbl_image WHERE id = '" + imageLocalID +"'";
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
			String sqlString = "DELETE FROM tbl_others_image WHERE server_id = '" + imageServerID +"'";
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
	
	public Image getImageByLocalID(int imageLocalID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_image WHERE id = ?", 
											new String[]{Integer.toString(imageLocalID)});

			if (cursor.moveToNext())
			{
				Image image = new Image();
				image.setLocalID(getIntFromCursor(cursor, "id"));
				image.setServerID(getIntFromCursor(cursor, "server_id"));
                image.setServerPath(getStringFromCursor(cursor, "server_path"));
				image.setLocalPath(getStringFromCursor(cursor, "local_path"));
				image.setItemID(getIntFromCursor(cursor, "item_local_id"));

				cursor.close();
				return image;
			}
			else
			{
				cursor.close();
				return null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public Image getImageByServerID(int imageServerID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_image WHERE server_id = ?", 
											new String[]{Integer.toString(imageServerID)});

			if (cursor.moveToNext())
			{
				Image image = new Image();
				image.setLocalID(getIntFromCursor(cursor, "id"));
				image.setServerID(getIntFromCursor(cursor, "server_id"));
                image.setServerPath(getStringFromCursor(cursor, "server_path"));
				image.setLocalPath(getStringFromCursor(cursor, "local_path"));
				image.setItemID(getIntFromCursor(cursor, "item_local_id"));

				cursor.close();
				return image;
			}
			else
			{
				cursor.close();
				return null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public Image getOthersImage(int imageServerID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_image WHERE server_id = ?", 
											new String[]{Integer.toString(imageServerID)});

			if (cursor.moveToNext())
			{
				Image image = new Image();
				image.setLocalID(getIntFromCursor(cursor, "id"));
				image.setServerID(getIntFromCursor(cursor, "server_id"));
                image.setServerPath(getStringFromCursor(cursor, "server_path"));
				image.setLocalPath(getStringFromCursor(cursor, "local_path"));
				image.setItemID(getIntFromCursor(cursor, "item_server_id"));

				cursor.close();
				return image;
			}
			else
			{
				cursor.close();
				return null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Image> getItemImages(int itemLocalID)
	{
		List<Image> imageList = new ArrayList<Image>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_image WHERE item_local_id = ?", 
														new String[]{Integer.toString(itemLocalID)});

			while (cursor.moveToNext())
			{
				Image image = new Image();
				image.setLocalID(getIntFromCursor(cursor, "id"));
				image.setServerID(getIntFromCursor(cursor, "server_id"));
                image.setServerPath(getStringFromCursor(cursor, "server_path"));
				image.setLocalPath(getStringFromCursor(cursor, "local_path"));
				image.setItemID(getIntFromCursor(cursor, "item_local_id"));
				
				imageList.add(image);
			}
			
			cursor.close();
			return imageList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return imageList;
		}		
	}
	
	public List<Image> getOthersItemImages(int itemServerID)
	{
		List<Image> imageList = new ArrayList<Image>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_image WHERE item_server_id = ?", 
														new String[]{Integer.toString(itemServerID)});

			while (cursor.moveToNext())
			{
				Image image = new Image();
				image.setLocalID(getIntFromCursor(cursor, "id"));
				image.setServerID(getIntFromCursor(cursor, "server_id"));
                image.setServerPath(getStringFromCursor(cursor, "server_path"));
				image.setLocalPath(getStringFromCursor(cursor, "local_path"));
				image.setItemID(getIntFromCursor(cursor, "item_server_id"));
				
				imageList.add(image);
			}
			
			cursor.close();
			return imageList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return imageList;
		}		
	}
	
	// Auxiliaries
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

    private String sqliteEscape(String keyWord)
    {
        keyWord = keyWord.replace("/", "//");
        keyWord = keyWord.replace("'", "''");
        keyWord = keyWord.replace("[", "/[");
        keyWord = keyWord.replace("]", "/]");
        keyWord = keyWord.replace("%", "/%");
        keyWord = keyWord.replace("&","/&");
        keyWord = keyWord.replace("_", "/_");
        keyWord = keyWord.replace("(", "/(");
        keyWord = keyWord.replace(")", "/)");
        return keyWord;
    }
}
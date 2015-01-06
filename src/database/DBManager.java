package database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import classes.Category;
import classes.Comment;
import classes.Group;
import classes.Item;
import classes.Report;
import classes.Tag;
import classes.User;
import classes.Utils.AppPreference;
import classes.Utils.Utils;

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
		
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w("TaskDBAdapter", "Upgrading from version " + oldVersion + " to " + newVersion);
		
		// do upgrade here
		
		onCreate(db);
	}
	
	public static synchronized void createDBManager(Context context)
	{
		if (dbManager == null)
		{
			dbManager = new DBManager(context);
		}
	}
	
	public static synchronized DBManager getDBManager()
	{
		if (dbManager != null)
		{
			dbManager.openDatabase();
		}
		return dbManager;
	}

	public void executeTempCommand()
	{
//		String sqlString = "DELETE FROM tbl_report WHERE id = 23";
//		database.execSQL(sqlString);
//		String sqlString = "DROP TABLE IF EXISTS tbl_category";
//		database.execSQL(sqlString);
//		sqlString = "DROP TABLE IF EXISTS tbl_tag";
//		database.execSQL(sqlString);
//		sqlString = "DROP TABLE IF EXISTS tbl_category";
//		database.execSQL(sqlString);
//		sqlString = "DROP TABLE IF EXISTS tbl_item";
//		database.execSQL(sqlString);
//		sqlString = "DROP TABLE IF EXISTS tbl_comment";
//		database.execSQL(sqlString);	
//		sqlString = "DROP TABLE IF EXISTS tbl_others_report";
//		database.execSQL(sqlString);
//		sqlString = "DROP TABLE IF EXISTS tbl_others_item";
//		database.execSQL(sqlString);
//		sqlString = "DROP TABLE IF EXISTS tbl_others_comment";
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
			createTables();
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
	
	private boolean createTables()
	{
		try
		{
			String createItemTable="CREATE TABLE IF NOT EXISTS tbl_item ("
									+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
									+ "server_id INT DEFAULT(0),"
									+ "invoice_id INT DEFAULT(0),"
									+ "invoice_path TEXT DEFAULT(''),"
									+ "vendor TEXT DEFAULT(''),"
									+ "report_local_id INT DEFAULT(0),"
									+ "category_id INT DEFAULT(0),"
									+ "amount FLOAT DEFAULT(0),"
									+ "pa_amount FLOAT DEFAULT(0),"
									+ "user_id INT DEFAULT(0),"
									+ "consumed_date INT DEFAULT(0),"
									+ "note TEXT DEFAULT(''),"
									+ "prove_ahead INT DEFAULT(0),"
									+ "need_reimbursed INT DEFAULT(0),"
									+ "pa_approved INT DEFAULT(0),"
									+ "status INT DEFAULT(0),"
									+ "location TEXT DEFAULT(''),"
									+ "createdt INT DEFAULT(0),"
									+ "server_updatedt INT DEFAULT(0),"
									+ "local_updatedt INT DEFAULT(0),"
									+ "backup1 INT DEFAULT(0),"
									+ "backup2 TEXT DEFAULT(''),"
									+ "backup3 TEXT DEFAULT('')"
									+ ")";
			database.execSQL(createItemTable);

			String createItemUserTable="CREATE TABLE IF NOT EXISTS tbl_item_user ("
											+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
											+ "item_local_id INT DEFAULT(0),"
											+ "user_id INT DEFAULT(0),"
											+ "local_updatedt INT DEFAULT(0),"
											+ "backup1 INT DEFAULT(0),"
											+ "backup2 TEXT DEFAULT(''),"
											+ "backup3 TEXT DEFAULT('')"
											+ ")";
			database.execSQL(createItemUserTable);

			String createItemTagTable="CREATE TABLE IF NOT EXISTS tbl_item_tag ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "item_local_id INT DEFAULT(0),"
										+ "tag_id INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
			database.execSQL(createItemTagTable);
			
			String createOthersItemTable="CREATE TABLE IF NOT EXISTS tbl_others_item ("
									+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
									+ "server_id INT DEFAULT(0),"
									+ "invoice_id INT DEFAULT(0),"
									+ "invoice_path TEXT DEFAULT(''),"
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
									+ "prove_ahead INT DEFAULT(0),"
									+ "need_reimbursed INT DEFAULT(0),"
									+ "pa_approved INT DEFAULT(0),"
									+ "status INT DEFAULT(0),"
									+ "location TEXT DEFAULT(''),"
									+ "createdt INT DEFAULT(0),"
									+ "server_updatedt INT DEFAULT(0),"
									+ "local_updatedt INT DEFAULT(0),"
									+ "backup1 INT DEFAULT(0),"
									+ "backup2 TEXT DEFAULT(''),"
									+ "backup3 TEXT DEFAULT('')"
									+ ")";
			database.execSQL(createOthersItemTable);

			String createUserTable="CREATE TABLE IF NOT EXISTS tbl_user ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "server_id INT DEFAULT(0),"
										+ "email TEXT DEFAULT(''),"
										+ "phone TEXT DEFAULT(''),"
										+ "nickname TEXT DEFAULT(''),"
										+ "avatar_id INT DEFAULT(0),"
										+ "avatar_path TEXT DEFAULT(''),"
										+ "privilege INT DEFAULT(0),"
										+ "manager_id INT DEFAULT(0),"
										+ "group_id INT DEFAULT(0),"
										+ "admin INT DEFAULT(0),"
										+ "server_updatedt INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
			database.execSQL(createUserTable);

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
			database.execSQL(createGroupTable);
			
			String createReportTable="CREATE TABLE IF NOT EXISTS tbl_report ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "server_id INT DEFAULT(0),"
										+ "title TEXT DEFAULT(''),"
										+ "user_id INT DEFAULT(0),"
										+ "manager_id TEXT DEFAULT(''),"
										+ "cc_id TEXT DEFAULT(''),"
										+ "status INT DEFAULT(0),"
										+ "prove_ahead INT DEFAULT(0),"
										+ "created_date INT DEFAULT(0),"
										+ "server_updatedt INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
			database.execSQL(createReportTable);
			
			String createOthersReportTable="CREATE TABLE IF NOT EXISTS tbl_others_report ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "server_id INT DEFAULT(0),"
										+ "owner_id INT DEFAULT(0),"
										+ "title TEXT DEFAULT(''),"
										+ "user_id INT DEFAULT(0),"
										+ "manager_id TEXT DEFAULT(''),"
										+ "cc_id TEXT DEFAULT(''),"
										+ "status INT DEFAULT(0),"
										+ "prove_ahead INT DEFAULT(0),"
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
			database.execSQL(createOthersReportTable);

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
			database.execSQL(createCommentTable);

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
			database.execSQL(createOthersCommentTable);

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
			database.execSQL(createTagTable);

			String createCategoryTable="CREATE TABLE IF NOT EXISTS tbl_category ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "server_id INT DEFAULT(0),"
										+ "category_name TEXT DEFAULT(''),"
										+ "max_limit INT DEFAULT(0),"
										+ "group_id INT DEFAULT(0),"
										+ "parent_id INT DEFAULT(0),"
										+ "icon_id INT DEFAULT(0),"
										+ "icon_path TEXT DEFAULT(''),"
										+ "prove_ahead INT DEFAULT(0),"
										+ "server_updatedt INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
			database.execSQL(createCategoryTable);
			
			return true;
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
			String sqlString = "INSERT INTO tbl_user (server_id, email, phone, nickname, avatar_id, avatar_path, privilege, manager_id, " +
								"group_id, admin, local_updatedt, server_updatedt) VALUES (" +
								"'" + user.getServerID() + "'," +
								"'" + user.getEmail() + "'," +
								"'" + user.getPhone() + "'," +
								"'" + user.getNickname() + "'," +
								"'" + user.getAvatarID() + "'," +
								"'" + user.getAvatarPath() + "'," +
								"'" + user.getPrivilege() + "'," +
								"'" + user.getDefaultManagerID() + "'," +
								"'" + user.getGroupID() + "'," +
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
								"nickname = '" + user.getNickname() + "'," +
								"avatar_id = '" + user.getAvatarID() + "'," +
								"avatar_path = '" + user.getAvatarPath() + "'," +
								"manager_id = '" + user.getDefaultManagerID() + "'," +
								"group_id = '" + user.getGroupID() + "'," +
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
					user.setAvatarPath(localUser.getAvatarPath());
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
			Cursor cursor = database.rawQuery("SELECT server_id, email, phone, nickname, avatar_id, avatar_path, privilege, " +
											  "manager_id, group_id, admin, local_updatedt, server_updatedt " +
					                          "FROM tbl_user WHERE server_id = ?", new String[]{Integer.toString(userServerID)});
			if (cursor.moveToNext())
			{
				User user = new User();
				user.setServerID(getIntFromCursor(cursor, "server_id"));
				user.setEmail(getStringFromCursor(cursor, "email"));
				user.setPhone(getStringFromCursor(cursor, "phone"));
				user.setNickname(getStringFromCursor(cursor, "nickname"));
				user.setAvatarID(getIntFromCursor(cursor, "avatar_id"));
				user.setAvatarPath(getStringFromCursor(cursor, "avatar_path"));
				user.setPrivilege(getIntFromCursor(cursor, "privilege"));
				user.setDefaultManagerID(getIntFromCursor(cursor, "manager_id"));
				user.setGroupID(getIntFromCursor(cursor, "group_id"));
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
	
	public boolean insertUserList(List<User> userList)
	{
		try
		{
			for (int i = 0; i < userList.size(); i++)
			{
				insertUser(userList.get(i));
			}
			return true;
		}
		catch (Exception e)
		{
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
					if (localUser.getServerID() == user.getServerID() && localUser.getAvatarID() == user.getAvatarID())
					{
						user.setAvatarPath(localUser.getAvatarPath());
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

			Cursor cursor = database.rawQuery("SELECT server_id, email, phone, nickname, avatar_id, avatar_path, privilege, manager_id, " +
											  "group_id, admin, local_updatedt, server_updatedt " +
					                          "FROM tbl_user WHERE group_id = ?", new String[]{Integer.toString(groupServerID)});
			while (cursor.moveToNext())
			{
				User user = new User();
				user.setServerID(getIntFromCursor(cursor, "server_id"));
				user.setEmail(getStringFromCursor(cursor, "email"));
				user.setPhone(getStringFromCursor(cursor, "phone"));
				user.setNickname(getStringFromCursor(cursor, "nickname"));
				user.setAvatarID(getIntFromCursor(cursor, "avatar_id"));
				user.setAvatarPath(getStringFromCursor(cursor, "avatar_path"));
				user.setPrivilege(getIntFromCursor(cursor, "privilege"));
				user.setDefaultManagerID(getIntFromCursor(cursor, "manager_id"));
				user.setGroupID(getIntFromCursor(cursor, "group_id"));
				user.setIsAdmin(getBooleanFromCursor(cursor, "admin"));
				user.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				user.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				userList.add(user);
			}
			
			cursor.close();
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
			Cursor userCursor = database.rawQuery("SELECT user_id FROM tbl_item_user WHERE item_local_id=?", 
													new String[]{Integer.toString(itemLocalID)});
			while (userCursor.moveToNext())
			{
				User user = getUser(getIntFromCursor(userCursor, "user_id"));
				if (user != null)
				{
					relevantUsers.add(user);
				}
			}

			return relevantUsers.size() > 0? relevantUsers : null;
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
	public boolean insertItem(Item item)
	{
		try
		{
			System.out.println("insert item: local id = " + item.getLocalID() + ", server id = " + item.getServerID());
			int reportID = item.getBelongReport() == null ? -1 : item.getBelongReport().getLocalID();
			int categoryID = item.getCategory() == null ? -1 : item.getCategory().getServerID();			
			String sqlString = "INSERT INTO tbl_item (server_id, invoice_id, invoice_path, vendor, report_local_id, category_id, " +
							   							"amount, pa_amount, user_id, consumed_date, note, status, location, createdt, " +
							   							"server_updatedt, local_updatedt, prove_ahead, need_reimbursed, pa_approved) VALUES (" + 
														"'" + item.getServerID() + "'," +
														"'" + item.getInvoiceID() + "'," +
														"'" + item.getInvoicePath() + "'," +
														"'" + item.getVendor() + "'," +
														"'" + reportID + "'," +
														"'" + categoryID + "'," +
														"'" + item.getAmount() + "'," +
														"'" + item.getPaAmount() + "'," +
														"'" + item.getConsumer().getServerID() + "'," +
														"'" + item.getConsumedDate() + "'," +
														"'" + item.getNote() + "'," +
														"'" + item.getStatus() + "'," +
														"'" + item.getLocation() + "'," +
														"'" + item.getCreatedDate() + "'," +
														"'" + item.getServerUpdatedDate() + "'," +
														"'" + item.getLocalUpdatedDate() + "'," +
														"'" + Utils.booleanToInt(item.isProveAhead()) + "'," +
														"'" + Utils.booleanToInt(item.needReimbursed()) + "'," +
														"'" + Utils.booleanToInt(item.isPaApproved()) + "')";
			database.execSQL(sqlString);
			
			Cursor cursor = database.rawQuery("SELECT last_insert_rowid() from tbl_item", null);
			cursor.moveToFirst();
			item.setLocalID(cursor.getInt(0));

			updateItemTags(item);
			updateRelevantUsers(item);

			cursor.close();
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean insertOthersItem(Item item)
	{
		try
		{
			int categoryID = item.getCategory() == null ? -1 : item.getCategory().getServerID();			
			String sqlString = "INSERT INTO tbl_others_item (server_id, invoice_id, invoice_path, vendor, report_server_id, category_id, tags_id, " +
							   							"users_id, amount, pa_amount, user_id, consumed_date, note, status, location, createdt, " +
							   							"server_updatedt, local_updatedt, prove_ahead, need_reimbursed, pa_approved) VALUES (" + 
														"'" + item.getServerID() + "'," +
														"'" + item.getInvoiceID() + "'," +
														"'" + item.getInvoicePath() + "'," +
														"'" + item.getVendor() + "'," +
														"'" + item.getBelongReport().getServerID() + "'," +
														"'" + categoryID + "'," +
														"'" + item.getTagsID() + "'," +
														"'" + item.getRelevantUsersID() + "'," +
														"'" + item.getAmount() + "'," +
														"'" + item.getPaAmount() + "'," +
														"'" + item.getConsumer().getServerID() + "'," +
														"'" + item.getConsumedDate() + "'," +
														"'" + item.getNote() + "'," +
														"'" + item.getStatus() + "'," +
														"'" + item.getLocation() + "'," +
														"'" + item.getCreatedDate() + "'," +
														"'" + item.getServerUpdatedDate() + "'," +
														"'" + item.getLocalUpdatedDate() + "'," +
														"'" + Utils.booleanToInt(item.isProveAhead()) + "'," +
														"'" + Utils.booleanToInt(item.needReimbursed()) + "'," +
														"'" + Utils.booleanToInt(item.isPaApproved()) + "')";
			database.execSQL(sqlString);
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
			int reportID = item.getBelongReport() == null ? -1 : item.getBelongReport().getLocalID();
			int categoryID = item.getCategory() == null ? -1 : item.getCategory().getServerID();
			String sqlString = "UPDATE tbl_item SET " +
								"server_id = '" + item.getServerID() + "'," +
								"invoice_id = '" + item.getInvoiceID() + "'," +
								"invoice_path = '" + item.getInvoicePath() + "'," +
								"vendor = '" + item.getVendor() + "'," +
								"report_local_id = '" + reportID + "'," +
								"category_id = '" + categoryID + "'," +
								"amount = '" + item.getAmount() + "'," +
								"pa_amount = '" + item.getPaAmount() + "'," +
								"user_id = '" + item.getConsumer().getServerID() + "'," +
								"consumed_date = '" + item.getConsumedDate() + "'," +
								"note = '" + item.getNote() + "'," +
								"status = '" + item.getStatus() + "'," +
								"location = '" + item.getLocation() + "'," +
								"createdt = '" + item.getCreatedDate() + "'," +
								"server_updatedt = '" + item.getServerUpdatedDate() + "'," +
								"local_updatedt = '" + item.getLocalUpdatedDate() + "'," +
								"prove_ahead = '" + Utils.booleanToInt(item.isProveAhead()) + "'," +
								"need_reimbursed = '" + Utils.booleanToInt(item.needReimbursed()) + "'," +
								"pa_approved = '" + Utils.booleanToInt(item.isPaApproved()) + "' " +
								"WHERE id = '" + item.getLocalID() + "'";			
			database.execSQL(sqlString);
			
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
			int reportID = item.getBelongReport() == null ? -1 : item.getBelongReport().getLocalID();
			int categoryID = item.getCategory() == null ? -1 : item.getCategory().getServerID();
			String sqlString = "UPDATE tbl_item SET " +
								"server_id = '" + item.getServerID() + "'," +
								"invoice_id = '" + item.getInvoiceID() + "'," +
								"invoice_path = '" + item.getInvoicePath() + "'," +
								"vendor = '" + item.getVendor() + "'," +
								"report_local_id = '" + reportID + "'," +
								"category_id = '" + categoryID + "'," +
								"amount = '" + item.getAmount() + "'," +
								"pa_amount = '" + item.getPaAmount() + "'," +
								"user_id = '" + item.getConsumer().getServerID() + "'," +
								"consumed_date = '" + item.getConsumedDate() + "'," +
								"note = '" + item.getNote() + "'," +
								"status = '" + item.getStatus() + "'," +
								"location = '" + item.getLocation() + "'," +
								"createdt = '" + item.getCreatedDate() + "'," +
								"server_updatedt = '" + item.getServerUpdatedDate() + "'," +
								"local_updatedt = '" + item.getLocalUpdatedDate() + "'," +
								"prove_ahead = '" + Utils.booleanToInt(item.isProveAhead()) + "'," +
								"need_reimbursed = '" + Utils.booleanToInt(item.needReimbursed()) + "'," +
								"pa_approved = '" + Utils.booleanToInt(item.isPaApproved()) + "' " +
								"WHERE server_id = '" + item.getServerID() + "'";			
			database.execSQL(sqlString);
			
			if (item.getTags() != null || item.getRelevantUsers() != null)
			{
				item.setLocalID(getItemByServerID(item.getServerID()).getLocalID());
			}
			
			updateItemTags(item);
			updateRelevantUsers(item);

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
			String idString = remainingList.size() > 0 ? TextUtils.join(",", remainingList) + ",-1" : "-1";
			String sqlString = "DELETE FROM tbl_item WHERE server_id NOT IN (" + idString +") AND user_id = " + userServerID;
			database.execSQL(sqlString);
			
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
				return insertItem(item);
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
					return insertItem(item);
				}
				else if (item.getServerUpdatedDate() > localItem.getLocalUpdatedDate())
				{
					if (item.getInvoiceID() == localItem.getInvoiceID())
					{
						item.setInvoicePath(localItem.getInvoicePath());
					}
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
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE id=?", new String[]{Integer.toString(itemLocalID)});

			if (cursor.moveToNext())
			{
				Item item = new Item();
				item.setLocalID(getIntFromCursor(cursor, "id"));
				item.setServerID(getIntFromCursor(cursor, "server_id"));
				item.setInvoiceID(getIntFromCursor(cursor, "invoice_id"));
				item.setInvoicePath(getStringFromCursor(cursor, "invoice_path"));
				item.setVendor(getStringFromCursor(cursor, "vendor"));
				item.setAmount(getDoubleFromCursor(cursor, "amount"));
				item.setPaAmount(getDoubleFromCursor(cursor, "pa_amount"));
				item.setNote(getStringFromCursor(cursor, "note"));
				item.setStatus(getIntFromCursor(cursor, "status"));
				item.setLocation(getStringFromCursor(cursor, "location"));
				item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
				item.setCreatedDate(getIntFromCursor(cursor, "createdt"));
				item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				item.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
				item.setPaApproved(getBooleanFromCursor(cursor, "pa_approved"));
				item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
				item.setBelongReport(getReportByLocalID(getIntFromCursor(cursor, "report_local_id")));
				item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));				
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
	
	public Item getItemByServerID(int itemServerID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE server_id=?", new String[]{Integer.toString(itemServerID)});

			if (cursor.moveToNext())
			{
				Item item = new Item();
				item.setLocalID(getIntFromCursor(cursor, "id"));
				item.setServerID(getIntFromCursor(cursor, "server_id"));
				item.setInvoiceID(getIntFromCursor(cursor, "invoice_id"));
				item.setInvoicePath(getStringFromCursor(cursor, "invoice_path"));
				item.setVendor(getStringFromCursor(cursor, "vendor"));
				item.setAmount(getDoubleFromCursor(cursor, "amount"));
				item.setPaAmount(getDoubleFromCursor(cursor, "pa_amount"));
				item.setNote(getStringFromCursor(cursor, "note"));
				item.setStatus(getIntFromCursor(cursor, "status"));
				item.setLocation(getStringFromCursor(cursor, "location"));
				item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
				item.setCreatedDate(getIntFromCursor(cursor, "createdt"));
				item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				item.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
				item.setPaApproved(getBooleanFromCursor(cursor, "pa_approved"));
				item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
				item.setBelongReport(getReportByLocalID(getIntFromCursor(cursor, "report_local_id")));
				item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));				
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
				item.setInvoiceID(getIntFromCursor(cursor, "invoice_id"));
				item.setInvoicePath(getStringFromCursor(cursor, "invoice_path"));
				item.setVendor(getStringFromCursor(cursor, "vendor"));
				item.setAmount(getDoubleFromCursor(cursor, "amount"));
				item.setPaAmount(getDoubleFromCursor(cursor, "pa_amount"));
				item.setNote(getStringFromCursor(cursor, "note"));
				item.setStatus(getIntFromCursor(cursor, "status"));
				item.setLocation(getStringFromCursor(cursor, "location"));
				item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
				item.setCreatedDate(getIntFromCursor(cursor, "createdt"));
				item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				item.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
				item.setPaApproved(getBooleanFromCursor(cursor, "pa_approved"));
				item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
				item.setBelongReport(getOthersReport(getIntFromCursor(cursor, "report_server_id")));
				item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));
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
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE user_id=?", new String[]{Integer.toString(userServerID)});

			while (cursor.moveToNext())
			{
				Item item = new Item();
				item.setLocalID(getIntFromCursor(cursor, "id"));
				item.setServerID(getIntFromCursor(cursor, "server_id"));
				item.setInvoiceID(getIntFromCursor(cursor, "invoice_id"));
				item.setInvoicePath(getStringFromCursor(cursor, "invoice_path"));
				item.setVendor(getStringFromCursor(cursor, "vendor"));
				item.setAmount(getDoubleFromCursor(cursor, "amount"));
				item.setPaAmount(getDoubleFromCursor(cursor, "pa_amount"));
				item.setNote(getStringFromCursor(cursor, "note"));
				item.setStatus(getIntFromCursor(cursor, "status"));
				item.setLocation(getStringFromCursor(cursor, "location"));
				item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
				item.setCreatedDate(getIntFromCursor(cursor, "createdt"));
				item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				item.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
				item.setPaApproved(getBooleanFromCursor(cursor, "pa_approved"));
				item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
				item.setBelongReport(getReportByLocalID(getIntFromCursor(cursor, "report_local_id")));
				item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));
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
												"(prove_ahead = 0 OR (prove_ahead = 1 AND pa_approved = 1))", 
													new String[]{Integer.toString(userServerID)});

			while (cursor.moveToNext())
			{
				Item item = new Item();
				item.setLocalID(getIntFromCursor(cursor, "id"));
				item.setServerID(getIntFromCursor(cursor, "server_id"));
				item.setInvoiceID(getIntFromCursor(cursor, "invoice_id"));
				item.setInvoicePath(getStringFromCursor(cursor, "invoice_path"));
				item.setVendor(getStringFromCursor(cursor, "vendor"));
				item.setAmount(getDoubleFromCursor(cursor, "amount"));
				item.setPaAmount(getDoubleFromCursor(cursor, "pa_amount"));
				item.setNote(getStringFromCursor(cursor, "note"));
				item.setStatus(getIntFromCursor(cursor, "status"));
				item.setLocation(getStringFromCursor(cursor, "location"));
				item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
				item.setCreatedDate(getIntFromCursor(cursor, "createdt"));
				item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				item.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
				item.setPaApproved(getBooleanFromCursor(cursor, "pa_approved"));
				item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
				item.setBelongReport(getReportByLocalID(getIntFromCursor(cursor, "report_local_id")));
				item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));
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
	
	public List<Item> getUnarchivedProveAheadItems(int userServerID)
	{
		List<Item> itemList = new ArrayList<Item>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE user_id = ? AND " +
												"(report_local_id = -1 OR report_local_id = 0) AND " +
												"prove_ahead = 1 AND pa_approved = 1", 
													new String[]{Integer.toString(userServerID)});

			while (cursor.moveToNext())
			{
				Item item = new Item();
				item.setLocalID(getIntFromCursor(cursor, "id"));
				item.setServerID(getIntFromCursor(cursor, "server_id"));
				item.setInvoiceID(getIntFromCursor(cursor, "invoice_id"));
				item.setInvoicePath(getStringFromCursor(cursor, "invoice_path"));
				item.setVendor(getStringFromCursor(cursor, "vendor"));
				item.setAmount(getDoubleFromCursor(cursor, "amount"));
				item.setPaAmount(getDoubleFromCursor(cursor, "pa_amount"));
				item.setNote(getStringFromCursor(cursor, "note"));
				item.setStatus(getIntFromCursor(cursor, "status"));
				item.setLocation(getStringFromCursor(cursor, "location"));
				item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
				item.setCreatedDate(getIntFromCursor(cursor, "createdt"));
				item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				item.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
				item.setPaApproved(getBooleanFromCursor(cursor, "pa_approved"));
				item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
				item.setBelongReport(getReportByLocalID(getIntFromCursor(cursor, "report_local_id")));
				item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));
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
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE (local_updatedt > server_updatedt " +
											"OR (invoice_id = -1 AND invoice_path != '')) AND user_id = ?", 
													new String[]{Integer.toString(userServerID)});

			while (cursor.moveToNext())
			{
				Item item = new Item();
				item.setLocalID(getIntFromCursor(cursor, "id"));
				item.setServerID(getIntFromCursor(cursor, "server_id"));
				item.setInvoiceID(getIntFromCursor(cursor, "invoice_id"));
				item.setInvoicePath(getStringFromCursor(cursor, "invoice_path"));
				item.setVendor(getStringFromCursor(cursor, "vendor"));
				item.setAmount(getDoubleFromCursor(cursor, "amount"));
				item.setPaAmount(getDoubleFromCursor(cursor, "pa_amount"));
				item.setNote(getStringFromCursor(cursor, "note"));
				item.setStatus(getIntFromCursor(cursor, "status"));
				item.setLocation(getStringFromCursor(cursor, "location"));
				item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
				item.setCreatedDate(getIntFromCursor(cursor, "createdt"));
				item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				item.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
				item.setPaApproved(getBooleanFromCursor(cursor, "pa_approved"));
				item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
				item.setBelongReport(getReportByLocalID(getIntFromCursor(cursor, "report_local_id")));
				item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));
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
			Cursor cursor = database.rawQuery("SELECT id, server_id FROM tbl_item WHERE user_id=? AND server_id != -1", 
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
	
	public boolean deleteReportItem(int itemLocalID)
	{
		try
		{
			String sqlString = "UPDATE tbl_item SET " +
								"report_local_id = '" + -1 + "' " +
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
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE report_local_id=?", 
													new String[]{Integer.toString(reportLocalID)});

			while (cursor.moveToNext())
			{
				Item item = new Item();
				item.setLocalID(getIntFromCursor(cursor, "id"));
				item.setServerID(getIntFromCursor(cursor, "server_id"));
				item.setInvoiceID(getIntFromCursor(cursor, "invoice_id"));
				item.setInvoicePath(getStringFromCursor(cursor, "invoice_path"));
				item.setVendor(getStringFromCursor(cursor, "vendor"));
				item.setAmount(getDoubleFromCursor(cursor, "amount"));
				item.setPaAmount(getDoubleFromCursor(cursor, "pa_amount"));
				item.setNote(getStringFromCursor(cursor, "note"));
				item.setStatus(getIntFromCursor(cursor, "status"));
				item.setLocation(getStringFromCursor(cursor, "location"));
				item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
				item.setCreatedDate(getIntFromCursor(cursor, "createdt"));
				item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				item.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
				item.setPaApproved(getBooleanFromCursor(cursor, "pa_approved"));
				item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
				item.setBelongReport(getReportByLocalID(getIntFromCursor(cursor, "report_local_id")));
				item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));
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
				item.setInvoiceID(getIntFromCursor(cursor, "invoice_id"));
				item.setInvoicePath(getStringFromCursor(cursor, "invoice_path"));
				item.setVendor(getStringFromCursor(cursor, "vendor"));
				item.setAmount(getDoubleFromCursor(cursor, "amount"));
				item.setPaAmount(getDoubleFromCursor(cursor, "pa_amount"));
				item.setNote(getStringFromCursor(cursor, "note"));
				item.setStatus(getIntFromCursor(cursor, "status"));
				item.setLocation(getStringFromCursor(cursor, "location"));
				item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
				item.setCreatedDate(getIntFromCursor(cursor, "createdt"));
				item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				item.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
				item.setPaApproved(getBooleanFromCursor(cursor, "pa_approved"));
				item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
				item.setBelongReport(getOthersReport(reportServerID));
				item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));
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
	
	public int getLastInsertItemID()
	{
		Cursor cursor = database.rawQuery("SELECT last_insert_rowid() from tbl_item", null);
		cursor.moveToFirst();
		int result = cursor.getInt(0);
		cursor.close();
		return result;
	}
	
	// Report
	public boolean insertReport(Report report)
	{	
		try
		{
			String sqlString = "INSERT INTO tbl_report (server_id, title, user_id, status, manager_id, cc_id, prove_ahead, created_date, " +
							   							"server_updatedt, local_updatedt) VALUES (" + 
														"'" + report.getServerID() + "'," +
														"'" + report.getTitle() + "'," +
														"'" + report.getSender().getServerID() + "'," +
														"'" + report.getStatus() + "'," +
														"'" + User.getUsersIDString(report.getManagerList()) + "'," +
														"'" + User.getUsersIDString(report.getCCList()) + "'," +
														"'" + Utils.booleanToInt(report.isProveAhead()) + "'," +
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
	
	public boolean insertOthersReport(Report report)
	{
		try
		{
			String sqlString = "INSERT INTO tbl_others_report (server_id, owner_id, title, user_id, status, manager_id, cc_id, " +
									"prove_ahead, amount, item_count, is_cc, created_date, server_updatedt, local_updatedt) VALUES (" + 
								"'" + report.getServerID() + "'," +
								"'" + AppPreference.getAppPreference().getCurrentUserID() + "'," +
								"'" + report.getTitle() + "'," +
								"'" + report.getSender().getServerID() + "'," +
								"'" + report.getStatus() + "'," +
								"'" + User.getUsersIDString(report.getManagerList()) + "'," +
								"'" + User.getUsersIDString(report.getCCList()) + "'," +
								"'" + Utils.booleanToInt(report.isProveAhead()) + "'," +
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
			String sqlString = "UPDATE tbl_report SET " +
								"server_id = '" + report.getServerID() + "'," +
								"title = '" + report.getTitle() + "'," +
								"user_id = '" + report.getSender().getServerID() + "'," +
								"status = '" + report.getStatus() + "'," +
								"manager_id = '" + User.getUsersIDString(report.getManagerList()) + "'," +
								"cc_id = '" + User.getUsersIDString(report.getCCList()) + "'," +
								"prove_ahead = '" + Utils.booleanToInt(report.isProveAhead()) + "'," +
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
			String sqlString = "UPDATE tbl_report SET " +
								"server_id = '" + report.getServerID() + "'," +
								"title = '" + report.getTitle() + "'," +
								"user_id = '" + report.getSender().getServerID() + "'," +
								"status = '" + report.getStatus() + "'," +
								"manager_id = '" + User.getUsersIDString(report.getManagerList()) + "'," +
								"cc_id = '" + User.getUsersIDString(report.getCCList()) + "'," +
								"prove_ahead = '" + Utils.booleanToInt(report.isProveAhead()) + "'," +
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
			String sqlString = "UPDATE tbl_others_report SET " +
								"server_id = '" + report.getServerID() + "'," +
								"title = '" + report.getTitle() + "'," +
								"user_id = '" + report.getSender().getServerID() + "'," +
								"status = '" + report.getStatus() + "'," +
								"manager_id = '" + User.getUsersIDString(report.getManagerList()) + "'," +
								"cc_id = '" + User.getUsersIDString(report.getCCList()) + "'," +
								"prove_ahead = '" + Utils.booleanToInt(report.isProveAhead()) + "'," +
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
			String idString = remainingList.size() > 0 ? TextUtils.join(",", remainingList) + ",-1" : "-1";
			String sqlString = "DELETE FROM tbl_report WHERE server_id NOT IN (" + idString +") AND user_id = " + userServerID;
			database.execSQL(sqlString);
			
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
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_report WHERE id=? ", 
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
				report.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				report.setStatus(getIntFromCursor(cursor, "status"));
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
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_report WHERE server_id=? ", 
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
				report.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				report.setStatus(getIntFromCursor(cursor, "status"));
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
				report.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
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
				return insertReport(report);
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
					return insertReport(report);
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
				report.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
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
				report.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
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
				report.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
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
	
	public int getLastInsertReportID()
	{
		Cursor cursor = database.rawQuery("SELECT last_insert_rowid() from tbl_report", null);
		cursor.moveToFirst();
		int result = cursor.getInt(0);
		cursor.close();
		return result;
	}
	
	public double getReportAmount(int reportLocalID)
	{
		double amount = 0;
		Cursor cursor = database.rawQuery("SELECT amount FROM tbl_item WHERE report_local_id=?", 
												new String[]{Integer.toString(reportLocalID)});
		while (cursor.moveToNext())
		{
			amount += getDoubleFromCursor(cursor, "amount");
		}

		return amount;
	}
	
	public int getReportItemsCount(int reportLocalID)
	{
		int count = 0;
		Cursor cursor = database.rawQuery("SELECT amount FROM tbl_item WHERE report_local_id=?", 
												new String[]{Integer.toString(reportLocalID)});
		while (cursor.moveToNext())
		{
			count++;
		}

		cursor.close();
		return count;
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
								"'" + comment.getContent() + "'," +
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
								"'" + comment.getContent() + "'," +
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
								"comment = '" + comment.getContent() + "'," +
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
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_comment WHERE id=?", 
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
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_comment WHERE report_local_id=?", 
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
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_others_comment WHERE report_server_id=?", 
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
								"parent_id, icon_id, icon_path, prove_ahead, local_updatedt, server_updatedt) VALUES (" +
								"'" + category.getServerID() + "'," +
								"'" + category.getName() + "'," +
								"'" + category.getLimit() + "'," +
								"'" + category.getGroupID() + "'," +
								"'" + category.getParentID() + "'," +
								"'" + category.getIconID() + "'," +
								"'" + category.getIconPath() + "'," +
								"'" + Utils.booleanToInt(category.isProveAhead()) + "'," +
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
								"category_name = '" + category.getName() + "'," +
								"max_limit = '" + category.getLimit() + "'," +
								"group_id = '" + category.getGroupID() + "'," +
								"parent_id = '" + category.getParentID() + "'," +
								"icon_id = '" + category.getIconID() + "'," +
								"icon_path = '" + category.getIconPath() + "'," +
								"prove_ahead = '" + Utils.booleanToInt(category.isProveAhead()) + "'," +
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
	
	public boolean syncCategory(Category category)
	{
		try
		{
			Category localCategory = getCategory(category.getServerID());
			if (localCategory == null)
			{
				return insertCategory(category);
			}
			else if (category.getServerUpdatedDate() > localCategory.getLocalUpdatedDate())
			{
				return updateCategory(category);
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
				category.setIconPath(getStringFromCursor(cursor, "icon_path"));
				category.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
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
				category.setIconPath(getStringFromCursor(cursor, "icon_path"));
				category.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
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
				category.setIconPath(getStringFromCursor(cursor, "icon_path"));
				category.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
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
								"'" + tag.getName() + "'," +
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
								"tag_name = '" + tag.getName() + "'," +
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
			Cursor tagCursor = database.rawQuery("SELECT tag_id FROM tbl_item_tag WHERE item_local_id=?", 
													new String[]{Integer.toString(itemLocalID)});
			while (tagCursor.moveToNext())
			{
				Tag tag = getTag(getIntFromCursor(tagCursor, "tag_id"));
				if (tag != null)
				{
					tags.add(tag);
				}
			}
			
			return tags.size() > 0 ? tags : null;
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
	
	// Group
	public boolean insertGroup(Group group)
	{
		try
		{
			String sqlString = "INSERT INTO tbl_group (server_id, group_name, local_updatedt, server_updatedt) VALUES (" +
								"'" + group.getServerID() + "'," +
								"'" + group.getName() + "'," +
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
								"group_name = '" + group.getName() + "'," +
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
}
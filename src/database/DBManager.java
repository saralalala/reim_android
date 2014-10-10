package database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.util.Log;
import classes.Category;
import classes.Group;
import classes.Item;
import classes.Report;
import classes.Tag;
import classes.User;
import classes.Utils;

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
	
	public static synchronized DBManager getDataBaseManager(Context context)
	{
		if (dbManager == null)
		{
			dbManager = new DBManager(context);
		}
		dbManager.openDatabase();
		return dbManager;
	}

	public void tempCommand()
	{
//		String sqlString = "DROP TABLE IF EXISTS tbl_group";
//		database.execSQL(sqlString);
//		sqlString = "DROP TABLE IF EXISTS tbl_tag";
//		database.execSQL(sqlString);
//		sqlString = "DROP TABLE IF EXISTS tbl_item_user";
//		database.execSQL(sqlString);		
	}
	
	public Boolean openDatabase()
	{
		try
		{
			if (database == null)
			{
				database = getWritableDatabase();				
			}
			tempCommand();
			createTables();
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public Boolean closeDatabase()
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
	
	private Boolean createTables()
	{
		try
		{
			String createItemTable="CREATE TABLE IF NOT EXISTS tbl_item ("
									+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
									+ "server_id INT DEFAULT(0),"
									+ "image_id INT DEFAULT(0),"
									+ "invoice_path TEXT DEFAULT(''),"
									+ "merchant TEXT DEFAULT(''),"
									+ "report_id INT DEFAULT(0),"
									+ "category_id INT DEFAULT(0),"
									+ "amount FLOAT DEFAULT(0),"
									+ "user_id INT DEFAULT(0),"
									+ "consumed_date INT DEFAULT(0),"
									+ "note TEXT DEFAULT(''),"
									+ "server_updatedt INT DEFAULT(0),"
									+ "local_updatedt INT DEFAULT(0),"
									+ "prove_ahead INT DEFAULT(0),"
									+ "need_reimbursed INT DEFAULT(0),"
									+ "backup1 INT DEFAULT(0),"
									+ "backup2 TEXT DEFAULT(''),"
									+ "backup3 TEXT DEFAULT('')"
									+ ")";
			database.execSQL(createItemTable);

			String createItemUserTable="CREATE TABLE IF NOT EXISTS tbl_item_user ("
											+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
											+ "item_id INT DEFAULT(0),"
											+ "user_id INT DEFAULT(0),"
											+ "local_updatedt INT DEFAULT(0),"
											+ "backup1 INT DEFAULT(0),"
											+ "backup2 TEXT DEFAULT(''),"
											+ "backup3 TEXT DEFAULT('')"
											+ ")";
			database.execSQL(createItemUserTable);

			String createItemTagTable="CREATE TABLE IF NOT EXISTS tbl_item_tag ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "item_id INT DEFAULT(0),"
										+ "tag_id INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
			database.execSQL(createItemTagTable);

			String createUserTable="CREATE TABLE IF NOT EXISTS tbl_user ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "server_id INT DEFAULT(0),"
										+ "email TEXT DEFAULT(''),"
										+ "phone TEXT DEFAULT(''),"
										+ "nickname TEXT DEFAULT(''),"
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
										+ "manager_id INT DEFAULT(0),"
										+ "status INT DEFAULT(0),"
										+ "server_updatedt INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
			database.execSQL(createReportTable);

			String createReportCommentTable="CREATE TABLE IF NOT EXISTS tbl_report_comment ("
											+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
											+ "server_id INT DEFAULT(0),"
											+ "report_id INT DEFAULT(0),"
											+ "user_id INT DEFAULT(0),"
											+ "comment TEXT DEFAULT(''),"
											+ "local_updatedt INT DEFAULT(0),"
											+ "backup1 INT DEFAULT(0),"
											+ "backup2 TEXT DEFAULT(''),"
											+ "backup3 TEXT DEFAULT('')"
											+ ")";
			database.execSQL(createReportCommentTable);

			String createTagTable="CREATE TABLE IF NOT EXISTS tbl_tag ("
									+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
									+ "server_id INT DEFAULT(0),"
									+ "tag_name TEXT DEFAULT(''),"
									+ "group_id INT DEFAULT(0),"
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
	public Boolean insertUser(User user)
	{
		try
		{
			String sqlString = "INSERT INTO tbl_user (server_id, email, phone, nickname, privilege, manager_id, " +
								"group_id, admin, local_updatedt, server_updatedt) VALUES (" +
								"'" + user.getId() + "'," +
								"'" + user.getEmail() + "'," +
								"'" + user.getPhone() + "'," +
								"'" + user.getNickname() + "'," +
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
			System.out.println(e);
			return false;
		}
	}

	public Boolean updateUser(User user)
	{
		try
		{
			String sqlString = "UPDATE tbl_user SET " +
								"server_id = '" + user.getId() + "'," +
								"email = '" + user.getEmail() + "'," +
								"phone = '" + user.getPhone() + "'," +
								"nickname = '" + user.getNickname() + "'," +
								"manager_id = '" + user.getDefaultManagerID() + "'," +
								"group_id = '" + user.getGroupID() + "'," +
								"admin = '" + Utils.booleanToInt(user.isAdmin()) + "'," +
								"local_updatedt = '" + user.getLocalUpdatedDate() + "'," +
								"server_updatedt = '" + user.getServerUpdatedDate() + "' " +
								"WHERE server_id = '" + user.getId() + "'";			
			database.execSQL(sqlString);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public Boolean deleteUser(int userID)
	{
		try
		{
			String sqlString = "DELETE FROM tbl_user WHERE server_id = '" + userID + "'";			
			database.execSQL(sqlString);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public Boolean syncUser(User user)
	{
		try
		{
			User localUser = getUser(user.getId());
			if (localUser == null)
			{
				return insertUser(user);
			}
			else if (user.getServerUpdatedDate() > localUser.getLocalUpdatedDate())
			{
				return updateUser(user);
			}
			else
			{
				return true;
			}
		}
		catch (Exception e)
		{
			Log.i("reim", e.getMessage());
			return false;
		}
	}
	
	public User getUser(int userID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT server_id, email, phone, nickname, privilege, manager_id, " +
											  "group_id, admin, local_updatedt, server_updatedt " +
					                          "FROM tbl_user WHERE server_id = ?", new String[]{Integer.toString(userID)});
			if (cursor.moveToNext())
			{
				User user = new User();
				user.setId(getIntFromCursor(cursor, "server_id"));
				user.setEmail(getStringFromCursor(cursor, "email"));
				user.setPhone(getStringFromCursor(cursor, "phone"));
				user.setNickname(getStringFromCursor(cursor, "nickname"));
				user.setPrivilege(getIntFromCursor(cursor, "privilege"));
				user.setDefaultManagerID(getIntFromCursor(cursor, "manager_id"));
				user.setGroupID(getIntFromCursor(cursor, "group_id"));
				user.setIsAdmin(getBooleanFromCursor(cursor, "admin"));
				user.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				user.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				return user;
			}
			else
			{
				return null;				
			}
		}
		catch (Exception e)
		{
			Log.i("reim", e.toString());
			return null;
		}
	}
	
	public Boolean insertUserList(List<User> userList)
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

	public Boolean updateGroupUsers(List<User> userList, int groupID)
	{
		try
		{
			deleteGroupUsers(groupID);
			insertUserList(userList);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public List<User> getGroupUsers(int groupID)
	{
		List<User> userList = new ArrayList<User>();
		try
		{

			Cursor cursor = database.rawQuery("SELECT server_id, email, phone, nickname, privilege, manager_id, " +
											  "group_id, admin, local_updatedt, server_updatedt " +
					                          "FROM tbl_user WHERE group_id = ?", new String[]{Integer.toString(groupID)});
			while (cursor.moveToNext())
			{
				User user = new User();
				user.setId(getIntFromCursor(cursor, "server_id"));
				user.setEmail(getStringFromCursor(cursor, "email"));
				user.setPhone(getStringFromCursor(cursor, "phone"));
				user.setNickname(getStringFromCursor(cursor, "nickname"));
				user.setPrivilege(getIntFromCursor(cursor, "privilege"));
				user.setDefaultManagerID(getIntFromCursor(cursor, "manager_id"));
				user.setGroupID(getIntFromCursor(cursor, "group_id"));
				user.setIsAdmin(getBooleanFromCursor(cursor, "admin"));
				user.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				user.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				userList.add(user);
			}
			return userList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return userList;
		}
	}
	
	public Boolean deleteGroupUsers(int groupID)
	{
		try
		{
			String sqlString = "DELETE FROM tbl_user WHERE group_id = '" + groupID + "'";			
			database.execSQL(sqlString);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public Boolean insertRelevantUsers(Item item)
	{
		try
		{
			int count = item.getRelevantUsers().size();
			for (int i = 0; i < count; i++)
			{
				String sqlString = "INSERT INTO tbl_item_user (item_id, user_id, local_updatedt) VALUES (" +
									"'" + item.getLocalID() + "'," +
									"'" + item.getRelevantUsers().get(i).getId() + "'," +
									"'" + Utils.getCurrentTime() + "')";
				database.execSQL(sqlString);
			}			
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public List<User> getRelevantUsers(int itemLocalID)
	{
		try
		{
			List<User> relevantUsers = new ArrayList<User>();			
			Cursor userCursor = database.rawQuery("SELECT user_id FROM tbl_item_user WHERE item_id=?", 
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
			return null;
		}
	}
	
	public Boolean deleteRelevantUsers(int itemLocalID)
	{
		try
		{
			String sqlString = "DELETE FROM tbl_item_user WHERE item_id = '" + itemLocalID + "'";			
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
	public Boolean insertItem(Item item)
	{
		try
		{
			int reportID = item.getBelongReport() == null ? -1 : item.getBelongReport().getId();
			int categoryID = item.getCategory() == null ? -1 : item.getCategory().getId();			
			String sqlString = "INSERT INTO tbl_item (server_id, image_id, invoice_path, merchant, report_id, " +
							   							"category_id, amount, user_id, consumed_date, note, " +
							   							"server_updatedt, local_updatedt, prove_ahead, need_reimbursed) VALUES (" + 
														"'" + item.getId() + "'," +
														"'" + item.getImageID() + "'," +
														"'" + item.getInvoicePath() + "'," +
														"'" + item.getMerchant() + "'," +
														"'" + reportID + "'," +
														"'" + categoryID + "'," +
														"'" + item.getAmount() + "'," +
														"'" + item.getConsumer().getId() + "'," +
														"'" + item.getConsumedDate() + "'," +
														"'" + item.getNote() + "'," +
														"'" + item.getServerUpdatedDate() + "'," +
														"'" + item.getLocalUpdatedDate() + "'," +
														"'" + Utils.booleanToInt(item.isProveAhead()) + "'," +
														"'" + Utils.booleanToInt(item.needReimbursed()) + "')";
			database.execSQL(sqlString);
			
			Cursor cursor = database.rawQuery("SELECT last_insert_rowid() from tbl_item", null);
			cursor.moveToFirst();
			item.setLocalID(cursor.getInt(0));

			insertItemTags(item);
			insertRelevantUsers(item);
			
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public Boolean updateItem(Item item)
	{
		try
		{
			int reportID = item.getBelongReport() == null ? -1 : item.getBelongReport().getId();
			int categoryID = item.getCategory() == null ? -1 : item.getCategory().getId();
			String sqlString = "UPDATE tbl_item SET " +
								"server_id = '" + item.getId() + "'," +
								"image_id = '" + item.getImageID() + "'," +
								"invoice_path = '" + item.getInvoicePath() + "'," +
								"merchant = '" + item.getMerchant() + "'," +
								"report_id = '" + reportID + "'," +
								"category_id = '" + categoryID + "'," +
								"amount = '" + item.getAmount() + "'," +
								"user_id = '" + item.getConsumer().getId() + "'," +
								"consumed_date = '" + item.getConsumedDate() + "'," +
								"note = '" + item.getNote() + "'," +
								"server_updatedt = '" + item.getServerUpdatedDate() + "'," +
								"local_updatedt = '" + item.getLocalUpdatedDate() + "'," +
								"prove_ahead = '" + Utils.booleanToInt(item.isProveAhead()) + "'," +
								"need_reimbursed = '" + Utils.booleanToInt(item.needReimbursed()) + "' " +
								"WHERE id = '" + item.getLocalID() + "'";			
			database.execSQL(sqlString);
			
			deleteItemTags(item.getLocalID());
			insertItemTags(item);
			
			deleteRelevantUsers(item.getLocalID());
			insertRelevantUsers(item);

			return true;		
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public Boolean deleteItem(int itemLocalID)
	{
		try
		{
			String sqlString = "DELETE FROM tbl_item WHERE id = '" + itemLocalID +"'";
			database.execSQL(sqlString);

			sqlString = "DELETE FROM tbl_item_tag WHERE item_id = '" + itemLocalID +"'";
			database.execSQL(sqlString);
			
			sqlString = "DELETE FROM tbl_item_user WHERE item_id = '" + itemLocalID +"'";
			database.execSQL(sqlString);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public Boolean syncItem(Item item)
	{
		try
		{
			Item localItem = getItem(item.getLocalID());
			if (localItem == null)
			{
				return insertItem(item);
			}
			else
			{
				return updateItem(item);
			}
		}
		catch (Exception e)
		{
			Log.i("reim", e.getMessage());
			return false;
		}
	}
	
	public Item getItem(int itemLocalID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE id=?", new String[]{Integer.toString(itemLocalID)});

			if (cursor.moveToNext())
			{
				Item item = new Item();
				item.setLocalID(getIntFromCursor(cursor, "id"));
				item.setId(getIntFromCursor(cursor, "server_id"));
				item.setImageID(getIntFromCursor(cursor, "image_id"));
				item.setInvoicePath(getStringFromCursor(cursor, "invoice_path"));
				item.setMerchant(getStringFromCursor(cursor, "merchant"));
				item.setAmount(getDoubleFromCursor(cursor, "amount"));
				item.setNote(getStringFromCursor(cursor, "note"));
				item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
				item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				item.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
				item.setImage(BitmapFactory.decodeFile(item.getInvoicePath()));
				item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
				item.setBelongReport(getReport(getIntFromCursor(cursor, "report_id")));
				item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));				
				item.setRelevantUsers(getRelevantUsers(item.getLocalID()));
				item.setTags(getItemTags(item.getLocalID()));
				
				return item;
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Item> getUserItems(int userID)
	{
		try
		{
			List<Item> itemList = new ArrayList<Item>();
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE user_id=?", new String[]{Integer.toString(userID)});

			while (cursor.moveToNext())
			{
				Item item = new Item();
				item.setLocalID(getIntFromCursor(cursor, "id"));
				item.setId(getIntFromCursor(cursor, "server_id"));
				item.setImageID(getIntFromCursor(cursor, "image_id"));
				item.setInvoicePath(getStringFromCursor(cursor, "invoice_path"));
				item.setMerchant(getStringFromCursor(cursor, "merchant"));
				item.setAmount(getDoubleFromCursor(cursor, "amount"));
				item.setNote(getStringFromCursor(cursor, "note"));
				item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
				item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				item.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
				item.setImage(BitmapFactory.decodeFile(item.getInvoicePath()));
				item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
				item.setBelongReport(getReport(getIntFromCursor(cursor, "report_id")));
				item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));
				item.setRelevantUsers(getRelevantUsers(item.getLocalID()));
				item.setTags(getItemTags(item.getLocalID()));
				
				itemList.add(item);
			}
			
			return itemList;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public List<Item> getReportItems(int reportID)
	{
		try
		{
			List<Item> itemList = new ArrayList<Item>();
			Cursor cursor = database.rawQuery("SELECT * FROM tbl_item WHERE report_id=?", new String[]{Integer.toString(reportID)});

			while (cursor.moveToNext())
			{
				Item item = new Item();
				item.setLocalID(getIntFromCursor(cursor, "id"));
				item.setId(getIntFromCursor(cursor, "server_id"));
				item.setImageID(getIntFromCursor(cursor, "image_id"));
				item.setInvoicePath(getStringFromCursor(cursor, "invoice_path"));
				item.setMerchant(getStringFromCursor(cursor, "merchant"));
				item.setAmount(getDoubleFromCursor(cursor, "amount"));
				item.setNote(getStringFromCursor(cursor, "note"));
				item.setConsumedDate(getIntFromCursor(cursor, "consumed_date"));
				item.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				item.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				item.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				item.setNeedReimbursed(getBooleanFromCursor(cursor, "need_reimbursed"));
				item.setImage(BitmapFactory.decodeFile(item.getInvoicePath()));
				item.setConsumer(getUser(getIntFromCursor(cursor, "user_id")));
				item.setBelongReport(getReport(getIntFromCursor(cursor, "report_id")));
				item.setCategory(getCategory(getIntFromCursor(cursor, "category_id")));
				item.setRelevantUsers(getRelevantUsers(item.getLocalID()));
				item.setTags(getItemTags(item.getLocalID()));
				
				itemList.add(item);
			}
			
			return itemList;
		}
		catch (Exception e)
		{
			return null;
		}		
	}
	
	// Report
	public Boolean insertReport(Report report)
	{
		// TODO
		return true;		
	}
	
	public Boolean updateReport(Report report)
	{
		// TODO
//		if (group.getServerUpdatedDate() != -1)
//		{			
//		}
//		else
//		{
//			sqlString = "UPDATE tbl_group SET " +
//					"group_name = '" + group.getName() + "'," +
//					"local_updatedt = '" + group.getLocalUpdatedDate() + "' " +
//					"WHERE server_id = '" + group.getId() + "'";	
//		}
		return true;
	}
		
	public Boolean deleteReport(Report report)
	{
		// TODO
		return true;
	}

	public Report getReport(int reportID)
	{
		// TODO
		return null;
	}
	
	public List<Report> getUserReports(int userID)
	{
		// TODO implement
		return null;
	}
	
	public String getReportItemIDs(int reportID)
	{
		String itemIDString = "";
		Cursor cursor = database.rawQuery("SELECT server_id FROM tbl_item WHERE report_id=?", 
												new String[]{Integer.toString(reportID)});
		while (cursor.moveToNext())
		{
			itemIDString += getIntFromCursor(cursor, "server_id") + ",";
		}
		
		if (itemIDString.length() > 0)
		{
			itemIDString = itemIDString.substring(0, itemIDString.length()-1);
		}
		return itemIDString;
	}
	
	// Category
	public Boolean insertCategory(Category category)
	{
		try
		{
			String sqlString = "INSERT INTO tbl_category (server_id, category_name, max_limit, group_id, " +
								"parent_id, prove_ahead, local_updatedt, server_updatedt) VALUES (" +
								"'" + category.getId() + "'," +
								"'" + category.getName() + "'," +
								"'" + category.getLimit() + "'," +
								"'" + category.getGroupID() + "'," +
								"'" + category.getParentID() + "'," +
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
	
	public Boolean updateCategory(Category category)
	{
		try
		{
			String sqlString = "UPDATE tbl_category SET " +
								"category_name = '" + category.getName() + "'," +
								"max_limit = '" + category.getLimit() + "'," +
								"group_id = '" + category.getGroupID() + "'," +
								"parent_id = '" + category.getParentID() + "'," +
								"prove_ahead = '" + Utils.booleanToInt(category.isProveAhead()) + "'," +
								"local_updatedt = '" + category.getLocalUpdatedDate() + "'," +
								"server_updatedt = '" + category.getServerUpdatedDate() + "' " +
								"WHERE server_id = '" + category.getId() + "'";			
			database.execSQL(sqlString);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public Boolean deleteCategory(int categoryID)
	{
		try
		{
			String sqlString = "DELETE FROM tbl_category WHERE server_id = '" + categoryID + "'";			
			database.execSQL(sqlString);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public Boolean syncCategory(Category category)
	{
		try
		{
			Category localCategory = getCategory(category.getId());
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
	
	public Category getCategory(int categoryID)
	{
		try
		{	
			Cursor cursor = database.rawQuery("SELECT server_id, category_name, max_limit, group_id, " +
					                          "parent_id, prove_ahead, local_updatedt, server_updatedt " +
					                          "FROM tbl_category WHERE server_id = ?", new String[]{Integer.toString(categoryID)});
			if (cursor.moveToNext())
			{
				Category category = new Category();
				category.setId(getIntFromCursor(cursor, "server_id"));
				category.setName(getStringFromCursor(cursor, "category_name"));
				category.setLimit(getDoubleFromCursor(cursor, "max_limit"));
				category.setGroupID(getIntFromCursor(cursor, "group_id"));
				category.setParentID(getIntFromCursor(cursor, "parent_id"));
				category.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				category.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				category.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				return category;
			}
			else
			{
				return null;				
			}
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());;
			return null;
		}
	}
	
	public Boolean insertCategoryList(List<Category> categoryList)
	{
		try
		{
			for (int i = 0; i < categoryList.size(); i++)
			{
				insertCategory(categoryList.get(i));
			}
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public Boolean updateGroupCategories(List<Category> categoryList, int groupID)
	{
		try
		{
			deleteGroupCategories(groupID);
			insertCategoryList(categoryList);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public List<Category> getGroupCategories(int groupID)
	{
		List<Category> categoryList = new ArrayList<Category>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT server_id, category_name, max_limit, group_id, " +
					                          "parent_id, prove_ahead, local_updatedt, server_updatedt " +
					                          "FROM tbl_category WHERE group_id = ?", new String[]{Integer.toString(groupID)});
			while (cursor.moveToNext())
			{
				Category category = new Category();
				category.setId(getIntFromCursor(cursor, "server_id"));
				category.setName(getStringFromCursor(cursor, "category_name"));
				category.setLimit(getDoubleFromCursor(cursor, "max_limit"));
				category.setGroupID(getIntFromCursor(cursor, "group_id"));
				category.setParentID(getIntFromCursor(cursor, "parent_id"));
				category.setIsProveAhead(getBooleanFromCursor(cursor, "prove_ahead"));
				category.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				category.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				categoryList.add(category);
			}
			return categoryList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return categoryList;
		}
	}
	
	public Boolean deleteGroupCategories(int groupID)
	{
		try
		{
			String sqlString = "DELETE FROM tbl_category WHERE group_id = '" + groupID + "'";			
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
	public Boolean insertTag(Tag tag)
	{
		try
		{
			String sqlString = "INSERT INTO tbl_tag (server_id, tag_name, group_id, local_updatedt, server_updatedt) VALUES (" +
								"'" + tag.getId() + "'," +
								"'" + tag.getName() + "'," +
								"'" + tag.getGroupID() + "'," +
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
	
	public Boolean updateTag(Tag tag)
	{
		try
		{
			String sqlString = "UPDATE tbl_tag SET " +
								"tag_name = '" + tag.getName() + "'," +
								"group_id = '" + tag.getGroupID() + "'," +
								"local_updatedt = '" + tag.getLocalUpdatedDate() + "'," +
								"server_updatedt = '" + tag.getServerUpdatedDate() + "' " +
								"WHERE server_id = '" + tag.getId() + "'";			
			database.execSQL(sqlString);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public Boolean deleteTag(int tagID)
	{
		try
		{
			String sqlString = "DELETE FROM tbl_tag WHERE server_id = '" + tagID + "'";			
			database.execSQL(sqlString);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public Boolean syncTag(Tag tag)
	{
		try
		{
			Tag localTag = getTag(tag.getId());
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
	
	public Tag getTag(int tagID)
	{
		try
		{
			Cursor cursor = database.rawQuery("SELECT server_id, tag_name, group_id, local_updatedt, server_updatedt " +
					                          "FROM tbl_tag WHERE server_id = ?", new String[]{Integer.toString(tagID)});
			if (cursor.moveToNext())
			{
				Tag tag = new Tag();
				tag.setId(getIntFromCursor(cursor, "server_id"));
				tag.setName(getStringFromCursor(cursor, "tag_name"));
				tag.setGroupID(getIntFromCursor(cursor, "group_id"));
				tag.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				tag.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				return tag;
			}
			else
			{
				return null;				
			}
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());;
			return null;
		}
	}
	
	public Boolean insertTagList(List<Tag> tagList)
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
	
	public Boolean updateGroupTags(List<Tag> tagList, int groupID)
	{
		try
		{
			deleteGroupTags(groupID);
			insertTagList(tagList);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public List<Tag> getGroupTags(int groupID)
	{
		List<Tag> tagList = new ArrayList<Tag>();
		try
		{
			Cursor cursor = database.rawQuery("SELECT server_id, tag_name, group_id, local_updatedt, server_updatedt " +
												"FROM tbl_tag WHERE group_id = ?", new String[]{Integer.toString(groupID)});
			
			while (cursor.moveToNext())
			{
				Tag tag = new Tag();
				tag.setId(getIntFromCursor(cursor, "server_id"));
				tag.setName(getStringFromCursor(cursor, "tag_name"));
				tag.setGroupID(getIntFromCursor(cursor, "group_id"));
				tag.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				tag.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				tagList.add(tag);
			}
			return tagList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return tagList;
		}
	}
	
	public Boolean deleteGroupTags(int groupID)
	{
		try
		{
			String sqlString = "DELETE FROM tbl_tag WHERE group_id = '" + groupID + "'";			
			database.execSQL(sqlString);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}		
	}

	public Boolean insertItemTags(Item item)
	{
		try
		{
			int count = item.getTags().size();
			for (int i = 0; i < count; i++)
			{
				String sqlString = "INSERT INTO tbl_item_tag (item_id, tag_id, local_updatedt) VALUES (" +
									"'" + item.getLocalID() + "'," +
									"'" + item.getTags().get(i).getId() + "'," +
									"'" + Utils.getCurrentTime() + "')";
				database.execSQL(sqlString);
			}
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public List<Tag> getItemTags(int itemLocalID)
	{
		try
		{
			List<Tag> tags = new ArrayList<Tag>();			
			Cursor tagCursor = database.rawQuery("SELECT tag_id FROM tbl_item_tag WHERE item_id=?", 
													new String[]{Integer.toString(itemLocalID)});
			while (tagCursor.moveToNext())
			{
				Tag tag = getTag(getIntFromCursor(tagCursor, "tag_id"));
				if (tag != null)
				{
					tags.add(tag);
				}
			}
			
			return tags.size() > 0? tags : null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public Boolean deleteItemTags(int itemLocalID)
	{
		try
		{
			String sqlString = "DELETE FROM tbl_item_tag WHERE item_id = '" + itemLocalID + "'";			
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
	public Boolean insertGroup(Group group)
	{
		try
		{
			String sqlString = "INSERT INTO tbl_group (server_id, group_name, local_updatedt, server_updatedt) VALUES (" +
								"'" + group.getId() + "'," +
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
	
	public Boolean updateGroup(Group group)
	{
		try
		{
			String sqlString = "UPDATE tbl_group SET " +
					"group_name = '" + group.getName() + "'," +
					"local_updatedt = '" + group.getLocalUpdatedDate() + "'," +
					"server_updatedt = '" + group.getServerUpdatedDate() + "' " +
					"WHERE server_id = '" + group.getId() + "'";
			
			database.execSQL(sqlString);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}		
	}
	
	public Boolean deleteGroup(int groupID)
	{
		try
		{
			String sqlString = "DELETE FROM tbl_group WHERE server_id = '" + groupID + "'";			
			database.execSQL(sqlString);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public Boolean syncGroup(Group group)
	{
		try
		{
			Group localGroup = getGroup(group.getId());
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
	
	public Group getGroup(int groupID)
	{
		try
		{	
			Cursor cursor = database.rawQuery("SELECT server_id, group_name, local_updatedt, server_updatedt" +
											  " FROM tbl_group WHERE server_id = ?", new String[]{Integer.toString(groupID)});
			if (cursor.moveToNext())
			{
				Group group = new Group();
				group.setId(getIntFromCursor(cursor, "server_id"));
				group.setName(getStringFromCursor(cursor, "group_name"));
				group.setLocalUpdatedDate(getIntFromCursor(cursor, "local_updatedt"));
				group.setServerUpdatedDate(getIntFromCursor(cursor, "server_updatedt"));
				return group;
			}
			else
			{
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
	
	private Boolean getBooleanFromCursor(Cursor cursor, String columnName)
	{
		int temp=cursor.getInt(cursor.getColumnIndex(columnName));
		return temp > 0 ? true : false;
	}
}
package database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import classes.Category;
import classes.Group;
import classes.Item;
import classes.Report;

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
		return dbManager;
	}

	public Boolean openDatabase()
	{
		try
		{
			database = dbManager.getWritableDatabase();
//			tempCommand();
			createTables();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Boolean closeDatabase()
	{
		try
		{
			dbManager.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private Boolean createTables()
	{
		String createItemTable="CREATE TABLE IF NOT EXISTS tbl_item ("
								+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
								+ "server_id INT DEFAULT(0),"
								+ "amount FLOAT DEFAULT(0),"
								+ "merchants TEXT DEFAULT(''),"
								+ "category_id INT DEFAULT(0),"
								+ "image_id INT DEFAULT(0),"
								+ "user_id INT DEFAULT(0),"
								+ "billable INT DEFAULT(0),"
								+ "group_id INT DEFAULT(0),"
								+ "date INT DEFAULT(0),"
								+ "note TEXT DEFAULT(''),"
								+ "server_updatedt INT DEFAULT(0),"
								+ "local_updatedt INT DEFAULT(0),"
								+ "backup1 INT DEFAULT(0),"
								+ "backup2 TEXT DEFAULT(''),"
								+ "backup3 TEXT DEFAULT('')"
								+ ")";
		database.execSQL(createItemTable);

		String createItemUserTable="CREATE TABLE IF NOT EXISTS tbl_item_user ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "item_id INT DEFAULT(0),"
										+ "user_id INT DEFAULT(0),"
										+ "group_id INT DEFAULT(0),"
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

		String createImageTable="CREATE TABLE IF NOT EXISTS tbl_image ("
								+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
								+ "server_id INT DEFAULT(0),"
								+ "user_id INT DEFAULT(0),"
								+ "path TEXT DEFAULT(''),"
								+ "backup1 INT DEFAULT(0),"
								+ "backup2 TEXT DEFAULT(''),"
								+ "backup3 TEXT DEFAULT('')"
								+ ")";
		database.execSQL(createImageTable);

		String createUserTable="CREATE TABLE IF NOT EXISTS tbl_user ("
									+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
									+ "server_id INT DEFAULT(0),"
									+ "email TEXT DEFAULT(''),"
									+ "phone TEXT DEFAULT(''),"
									+ "nickname TEXT DEFAULT(''),"
									+ "privilege INT DEFAULT(0),"
									+ "manager_id INT DEFAULT(0),"
									+ "group_id INT DEFAULT(0),"							
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

		String createUserGroupTable="CREATE TABLE IF NOT EXISTS tbl_user_group ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "server_id INT DEFAULT(0),"
										+ "group_id INT DEFAULT(0),"
										+ "user_id INT DEFAULT(0),"
										+ "admin INT DEFAULT(0),"
										+ "server_updatedt INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
		database.execSQL(createUserGroupTable);

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

		String createReportItemTable="CREATE TABLE IF NOT EXISTS tbl_report_item ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "server_id INT DEFAULT(0),"
										+ "report_id INT DEFAULT(0),"
										+ "item_id INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
		database.execSQL(createReportItemTable);

		String createTagTable="CREATE TABLE IF NOT EXISTS tbl_tag ("
								+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
								+ "server_id INT DEFAULT(0),"
								+ "group_id INT DEFAULT(0),"
								+ "name TEXT DEFAULT(''),"
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
	
	// Item
	public Boolean insertItem(Item item)
	{
		String sqlString = "DELETE FROM tbl_item";
		database.execSQL(sqlString);
		sqlString = "INSERT INTO tbl_item (server_id, amount, billable) VALUES ('333', '5.5', '1')";
		database.execSQL(sqlString);
		sqlString = "INSERT INTO tbl_item (server_id, amount, billable) VALUES ('444', '6', '0')";
		database.execSQL(sqlString);
		sqlString = "INSERT INTO tbl_item (server_id, amount, billable) VALUES ('555', '6', '1')";
		database.execSQL(sqlString);
		return true;
	}

	public Boolean updateItem(Item oldItem, Item newItem)
	{
//		String sqlString = "UPDATE tbl_item SET amount = '6', billable = 'false' WHERE server_id = '333'";
		String sqlString = "UPDATE tbl_item SET merchants = 'McDonalds'";
		
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
		database.execSQL(sqlString);
		return true;		
	}

	public Boolean deleteItem(Item item)
	{
		String sqlString = "DELETE FROM tbl_item WHERE server_id = 'adf'";
		database.execSQL(sqlString);
		return true;
	}

	public List<Item> findMyItems()
	{
		List<Item> itemList = new ArrayList<Item>();
		Cursor cursor = database.rawQuery("SELECT server_id, amount, billable, merchants" +
				"						   FROM tbl_item WHERE amount=? and billable=?", new String[]{"6","true"});

		while (cursor.moveToNext())
		{
			Item item = new Item();
			item.setId(getIntFromCursor(cursor, "server_id"));
			item.setAmount(getDoubleFromCursor(cursor, "amount"));
			item.setBillable(getBooleanFromCursor(cursor, "billable"));
			item.setMerchant(getStringFromCursor(cursor, "merchants"));
			itemList.add(item);
		}
		
		return itemList;
	}
	
	// Report
	public Boolean insertReport(Report report)
	{
		return true;		
	}
	
	public Boolean updateReport(Report oldReport, Report newReport)
	{
		
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
		return true;
	}

	// Category
	public Boolean insertCategory(Category category)
	{
		try
		{
			int prove_ahead = category.isProveAhead() ? 1 : 0;
			String sqlString = "INSERT INTO tbl_category (server_id, category_name, max_limit, group_id, " +
								"parent_id, prove_ahead, local_updatedt, server_updatedt) VALUES (" +
								"'" + category.getId() + "'," +
								"'" + category.getName() + "'," +
								"'" + category.getLimit() + "'," +
								"'" + category.getGroupID() + "'," +
								"'" + category.getParentID() + "'," +
								"'" + prove_ahead + "'," +
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
			int prove_ahead = category.isProveAhead() ? 1 : 0;
			String sqlString = "UPDATE tbl_category SET " +
								"category_name = '" + category.getName() + "'," +
								"max_limit = '" + category.getLimit() + "'," +
								"group_id = '" + category.getGroupID() + "'," +
								"parent_id = '" + category.getParentID() + "'," +
								"prove_ahead = '" + prove_ahead + "'," +
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
	
	// Others
	public void tempCommand()
	{
		String sqlString = "DROP TABLE IF EXISTS tbl_category";
		database.execSQL(sqlString);
//		sqlString = "DROP TABLE IF EXISTS tbl_image";
//		database.execSQL(sqlString);
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
	
	private Boolean getBooleanFromCursor(Cursor cursor, String columnName)
	{
		int temp=cursor.getInt(cursor.getColumnIndex(columnName));
		return temp > 0 ? true : false;
	}
}
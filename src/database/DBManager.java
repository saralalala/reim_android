package database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import classes.Category;
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
								+ "billable BOOLEAN DEFAULT(false),"
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

		String createItemMemberTable="CREATE TABLE IF NOT EXISTS tbl_item_member ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "item_id INT DEFAULT(0),"
										+ "user_id INT DEFAULT(0),"
										+ "group_id INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
		database.execSQL(createItemMemberTable);

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
								+ "uid INT DEFAULT(0),"
								+ "path TEXT DEFAULT(''),"
								+ "backup1 INT DEFAULT(0),"
								+ "backup2 TEXT DEFAULT(''),"
								+ "backup3 TEXT DEFAULT('')"
								+ ")";
		database.execSQL(createImageTable);

		String createMemberTable="CREATE TABLE IF NOT EXISTS tbl_member ("
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
		database.execSQL(createMemberTable);

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

		String createMemberGroupTable="CREATE TABLE IF NOT EXISTS tbl_member_group ("
										+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
										+ "server_id INT DEFAULT(0),"
										+ "group_id INT DEFAULT(0),"
										+ "user_id INT DEFAULT(0),"
										+ "admin BOOLEAN DEFAULT(false),"
										+ "server_updatedt INT DEFAULT(0),"
										+ "local_updatedt INT DEFAULT(0),"
										+ "backup1 INT DEFAULT(0),"
										+ "backup2 TEXT DEFAULT(''),"
										+ "backup3 TEXT DEFAULT('')"
										+ ")";
		database.execSQL(createMemberGroupTable);

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
									+ "server_updatedt INT DEFAULT(0),"
									+ "local_updatedt INT DEFAULT(0),"
									+ "backup1 INT DEFAULT(0),"
									+ "backup2 TEXT DEFAULT(''),"
									+ "backup3 TEXT DEFAULT('')"
									+ ")";
		database.execSQL(createCategoryTable);
		
		return true;
	}
	
	public Boolean insertItem(Item item)
	{
		String sqlString = "DELETE FROM tbl_item";
		database.execSQL(sqlString);
		sqlString = "INSERT INTO tbl_item (server_id, amount, billable) VALUES ('333', '5.5', 'true')";
		database.execSQL(sqlString);
		sqlString = "INSERT INTO tbl_item (server_id, amount, billable) VALUES ('444', '6', 'false')";
		database.execSQL(sqlString);
		sqlString = "INSERT INTO tbl_item (server_id, amount, billable) VALUES ('555', '6', 'true')";
		database.execSQL(sqlString);
		return true;
	}

	public Boolean insertReport(Report report)
	{
		return true;		
	}
	
	public Boolean insertCategory(Category category)
	{
		return true;
	}
	
	public Boolean modifyItem(Item oldItem, Item newItem)
	{
//		String sqlString = "UPDATE tbl_item SET amount = '6', billable = 'false' WHERE server_id = '333'";
		String sqlString = "UPDATE tbl_item SET merchants = 'McDonalds'";
		database.execSQL(sqlString);
		return true;		
	}
	
	public Boolean modifyReport(Report oldReport, Report newReport)
	{
		return true;
	}
	
	public Boolean deleteItem(Item item)
	{
		String sqlString = "DELETE FROM tbl_item WHERE server_id = 'adf'";
		database.execSQL(sqlString);
		return true;
	}
	
	public Boolean deleteReport(Report report)
	{
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
	
	public void tempCommand()
	{
		String sqlString = "DROP TABLE tbl_company";
		database.execSQL(sqlString);
		sqlString = "DROP TABLE tbl_member_group";
		database.execSQL(sqlString);
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
		String tempString=cursor.getString(cursor.getColumnIndex(columnName));
		if (tempString.equals("true"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
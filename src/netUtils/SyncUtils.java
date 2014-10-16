package netUtils;

import java.util.List;

import android.util.SparseIntArray;

import netUtils.Request.SyncDataRequest;
import netUtils.Request.UploadImageRequest;
import netUtils.Request.Item.CreateItemRequest;
import netUtils.Request.Item.ModifyItemRequest;
import netUtils.Request.Report.CreateReportRequest;
import netUtils.Request.Report.ModifyReportRequest;
import netUtils.Response.SyncDataResponse;
import netUtils.Response.UploadImageResponse;
import netUtils.Response.Item.CreateItemResponse;
import netUtils.Response.Report.CreateReportResponse;
import netUtils.Response.Report.ModifyReportResponse;
import classes.AppPreference;
import classes.Item;
import classes.Report;
import database.DBManager;

public abstract class SyncUtils
{
	private static int itemTaskCount = 0;
	private static int reportTaskCount = 0;

	public static void syncFromServer(final SyncDataCallback callback)
	{
		SyncDataRequest request = new SyncDataRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				SyncDataResponse response = new SyncDataResponse(httpResponse);
				if (response.getStatus())
				{
					DBManager dbManager = DBManager.getDBManager();
					SparseIntArray reportIDArray = new SparseIntArray();
					for (Report report : response.getReportList())
					{
						 if (dbManager.syncReport(report))
						{
							Report tempReport = dbManager.getReportByServerID(report.getServerID());
							if (tempReport != null)
							{
								reportIDArray.put(tempReport.getServerID(), tempReport.getLocalID());								
							}
						}
					}
					
					for (Item item : response.getItemList())
					{
						Report report = item.getBelongReport();
						report.setLocalID(reportIDArray.get(report.getServerID()));
						item.setBelongReport(report);
						dbManager.syncItem(item);
					}
					
					callback.execute();
				}
			}
		});
	}
	
    public static void syncAllToServer(SyncDataCallback callback)
    {
    	itemTaskCount = 0;
    	reportTaskCount = 0;
    	AppPreference appPreference = AppPreference.getAppPreference();
    	DBManager dbManager = DBManager.getDBManager();
    	List<Item> itemList = dbManager.getUnsyncedItems(appPreference.getCurrentUserID());
    	itemTaskCount = itemList.size();
    	boolean flag = true;
    	for (Item item : itemList)
		{
			if (item.getImageID() == -1 && !item.getInvoicePath().equals(""))
			{
				flag = false;
				sendUploadImageRequest(item, callback);
			}
			else if (item.getServerID() == -1)
			{
				flag = true;
				sendCreateItemRequest(item, callback);
			}
			else
			{
				flag = true;
				sendUpdateItemRequest(item, callback);
			}
		}
    	if (flag)
		{
    		syncReportsToServer(callback);
		}
    }
    
    private static void syncReportsToServer(SyncDataCallback callback)
    {
    	AppPreference appPreference = AppPreference.getAppPreference();
    	DBManager dbManager = DBManager.getDBManager();
    	List<Report> reportList = dbManager.getUnsyncedUserReports(appPreference.getCurrentUserID());
    	reportTaskCount = reportList.size();
    	boolean flag = true;
		for (Report report : reportList)
		{
			flag = false;
			if (report.getServerID() == -1)
			{
				sendCreateReportRequest(report, callback);
			}
			else
			{
				sendUpdateReportRequest(report, callback);
			}
		}
		if (flag)
		{
			callback.execute();
		}
    }
        
    private static void sendCreateReportRequest(final Report report, final SyncDataCallback callback)
    {
    	CreateReportRequest request = new CreateReportRequest(report);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				CreateReportResponse response = new CreateReportResponse(httpResponse);
				if (response.getStatus())
				{
					report.setServerUpdatedDate(report.getLocalUpdatedDate());
					report.setServerID(response.getReportID());
					DBManager.getDBManager().updateReportByLocalID(report);
					reportTaskCount--;
					if (reportTaskCount == 0)
					{
						callback.execute();
					}
				}
				else
				{
					reportTaskCount--;
					if (reportTaskCount == 0)
					{
						callback.execute();
					}
				}
			}
		});
    }

    private static void sendUpdateReportRequest(final Report report, final SyncDataCallback callback)
    {
    	ModifyReportRequest request = new ModifyReportRequest(report);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				ModifyReportResponse response = new ModifyReportResponse(httpResponse);
				if (response.getStatus())
				{
					report.setServerUpdatedDate(report.getLocalUpdatedDate());
					DBManager.getDBManager().updateReportByLocalID(report);
					reportTaskCount--;
					if (reportTaskCount == 0)
					{
						callback.execute();
					}
				}
				else
				{
					reportTaskCount--;
					if (reportTaskCount == 0)
					{
						callback.execute();
					}
				}
			}
		});
    }
    
    private static void sendCreateItemRequest(final Item item, final SyncDataCallback callback)
    {
		CreateItemRequest request = new CreateItemRequest(item);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final CreateItemResponse response = new CreateItemResponse(httpResponse);
				if (response.getStatus())
				{
					item.setServerUpdatedDate(item.getLocalUpdatedDate());
					item.setServerID(response.getItemID());
					DBManager.getDBManager().updateItemByLocalID(item);
					itemTaskCount--;
					if (itemTaskCount == 0)
					{
						syncReportsToServer(callback);
					}
				}
				else
				{
					itemTaskCount--;
					if (itemTaskCount == 0)
					{
						syncReportsToServer(callback);
					}
				}
			}
		});
    }

    private static void sendUpdateItemRequest(final Item item, final SyncDataCallback callback)
    {
		ModifyItemRequest request = new ModifyItemRequest(item);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final CreateItemResponse response = new CreateItemResponse(httpResponse);
				if (response.getStatus())
				{
					item.setServerUpdatedDate(item.getLocalUpdatedDate());
					item.setServerID(response.getItemID());
					DBManager.getDBManager().updateItemByLocalID(item);
					itemTaskCount--;
					if (itemTaskCount == 0)
					{
						syncReportsToServer(callback);
					}
				}
				else
				{
					itemTaskCount--;
					if (itemTaskCount == 0)
					{
						syncReportsToServer(callback);
					}		
				}
			}
		});
    }
    
    private static void sendUploadImageRequest(final Item item, final SyncDataCallback callback)
    {
		UploadImageRequest request = new UploadImageRequest(item.getInvoicePath(), HttpConstant.IMAGE_TYPE_INVOICE);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final UploadImageResponse response = new UploadImageResponse(httpResponse);
				if (response.getStatus())
				{
					item.setImageID(response.getImageID());
					DBManager.getDBManager().updateItemByLocalID(item);
				}
				sendCreateItemRequest(item, callback);
			}
		});
    }

	public interface callback
	{
		void execute();
	}	
}
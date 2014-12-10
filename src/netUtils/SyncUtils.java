package netUtils;

import java.util.ArrayList;
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
import netUtils.Response.Item.ModifyItemResponse;
import netUtils.Response.Report.CreateReportResponse;
import netUtils.Response.Report.ModifyReportResponse;
import classes.AppPreference;
import classes.Item;
import classes.Report;
import classes.Utils;
import database.DBManager;

public abstract class SyncUtils
{
	public static boolean isSyncOnGoing = false;
	private static int itemTaskCount = 0;
	private static int reportTaskCount = 0;
	
	public static boolean canSyncToServer()
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		if (isSyncOnGoing)
		{
			return false;
		}
		else if (appPreference.syncOnlyWithWifi() && Utils.isWiFiConnected())
		{
			return true;
		}
		else if (!appPreference.syncOnlyWithWifi() && Utils.isNetworkConnected())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static void syncFromServer(final SyncDataCallback callback)
	{
//		int lastSynctime = AppPreference.getAppPreference().getLastSyncTime();
//		System.out.println("*************************************");
//		System.out.println("lastSynctime:"+lastSynctime);
		final int currentTime = Utils.getCurrentTime();
		SyncDataRequest request = new SyncDataRequest(0);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				SyncDataResponse response = new SyncDataResponse(httpResponse);
				if (response.getStatus())
				{
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setLastSyncTime(currentTime);
					appPreference.saveAppPreference();
					
					DBManager dbManager = DBManager.getDBManager();
					List<Integer> remainingList = new ArrayList<Integer>();
					SparseIntArray reportIDArray = new SparseIntArray();
					for (Report report : response.getReportList())
					{
						if (dbManager.syncReport(report))
						{
							remainingList.add(report.getServerID());
							Report tempReport = dbManager.getReportByServerID(report.getServerID());
							if (tempReport != null)
							{
								reportIDArray.put(tempReport.getServerID(), tempReport.getLocalID());								
							}
						}
					}
					dbManager.deleteTrashReports(remainingList, appPreference.getCurrentUserID());
					
					remainingList.clear();
//					int i = 1;
					for (Item item : response.getItemList())
					{
						Report report = item.getBelongReport();
						report.setLocalID(reportIDArray.get(report.getServerID()));
//						System.out.println("-------------------------------------------");
//						System.out.println("item server id:" + item.getServerID());
//						System.out.println("report server id:" + report.getServerID() + "    report local id:"+report.getLocalID());
//						System.out.println("number: "+i);
//						i++;
						item.setBelongReport(report);
						dbManager.syncItem(item);
						remainingList.add(item.getServerID());
					}
					
 					dbManager.deleteTrashItems(remainingList, appPreference.getCurrentUserID());

					if (callback != null)
					{
						callback.execute();						
					}
				}
				else
				{
					if (isSyncOnGoing)
					{
						isSyncOnGoing = false;						
					}
				}
			}
		});
	}
	
    public static void syncAllToServer(SyncDataCallback callback)
    {
		System.out.println("------------- syncAllToServer ------------");
    	itemTaskCount = 0;
    	reportTaskCount = 0;
    	AppPreference appPreference = AppPreference.getAppPreference();
    	DBManager dbManager = DBManager.getDBManager();
    	List<Item> itemList = dbManager.getUnsyncedItems(appPreference.getCurrentUserID());
    	itemTaskCount = itemList.size();
    	System.out.println("itemTaskCount："+itemTaskCount);
    	if (itemTaskCount > 0)
		{
        	for (Item item : itemList)
    		{
    			if (item.getInvoiceID() == -1 && !item.getInvoicePath().equals(""))
    			{
    				sendUploadImageRequest(item, callback);
    			}
    			else if (item.getServerID() == -1)
    			{
    				sendCreateItemRequest(item, callback);
    			}
    			else
    			{
    				sendUpdateItemRequest(item, callback);
    			}
    		}			
		}
    	else
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
    	System.out.println("reportTaskCount："+reportTaskCount);
    	if (reportTaskCount > 0)
		{
    		for (Report report : reportList)
    		{
    			if (!report.hasItems() || (report.getStatus() == Report.STATUS_SUBMITTED && !report.canBeSubmitted()))
				{
    		    	System.out.println("ignore report：local id " + report.getLocalID());
					reportTaskCount--;
					if (reportTaskCount == 0 && callback != null)
					{
						callback.execute();	
					}
					continue;
				}
    			
    			if (report.getServerID() == -1)
    			{
    				sendCreateReportRequest(report, callback);
    			}
    			else
    			{
    				sendUpdateReportRequest(report, callback);
    			}
    		}			
		}
    	else
		{
			if (callback != null)
			{
				callback.execute();				
			}
		}
    }
    
    private static void sendUploadImageRequest(final Item item, final SyncDataCallback callback)
    {
    	System.out.println("upload image for item：local id " + item.getLocalID());
		UploadImageRequest request = new UploadImageRequest(item.getInvoicePath(), HttpConstant.IMAGE_TYPE_INVOICE);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final UploadImageResponse response = new UploadImageResponse(httpResponse);
				if (response.getStatus())
				{
			    	System.out.println("upload image for item：local id " + item.getLocalID() + " *Succeed*");
					item.setInvoiceID(response.getImageID());
					DBManager.getDBManager().updateItem(item);
				}
				else
				{
			    	System.out.println("upload image for item：local id " + item.getLocalID() + " *Failed*");					
				}
				if (item.getServerID() == -1)
				{
					sendCreateItemRequest(item, callback);
				}
				else
				{
					sendUpdateItemRequest(item, callback);
				}
			}
		});
    }
    
    private static void sendCreateItemRequest(final Item item, final SyncDataCallback callback)
    {
    	System.out.println("create item：local id " + item.getLocalID());
		CreateItemRequest request = new CreateItemRequest(item);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final CreateItemResponse response = new CreateItemResponse(httpResponse);
				if (response.getStatus())
				{
			    	System.out.println("create item：local id " + item.getLocalID() + " *Succeed*");
					item.setLocalUpdatedDate(Utils.getCurrentTime());
					item.setServerUpdatedDate(item.getLocalUpdatedDate());
					item.setServerID(response.getItemID());
					item.setCreatedDate(response.getCreateDate());
					
					DBManager.getDBManager().updateItemByLocalID(item);
					itemTaskCount--;
					if (itemTaskCount == 0)
					{
						syncReportsToServer(callback);
					}
				}
				else
				{
			    	System.out.println("create item：local id " + item.getLocalID() + " *Failed*");
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
    	System.out.println("modify item：local id " + item.getLocalID());
		ModifyItemRequest request = new ModifyItemRequest(item);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ModifyItemResponse response = new ModifyItemResponse(httpResponse);
				if (response.getStatus())
				{
			    	System.out.println("modify item：local id " + item.getLocalID() + " *Succeed*");
					item.setLocalUpdatedDate(Utils.getCurrentTime());
					item.setServerUpdatedDate(item.getLocalUpdatedDate());
					item.setServerID(response.getItemID());
					DBManager.getDBManager().updateItem(item);
					itemTaskCount--;
					if (itemTaskCount == 0)
					{
						syncReportsToServer(callback);
					}
				}
				else
				{
			    	System.out.println("modify item：local id " + item.getLocalID() + " *Failed*");
					itemTaskCount--;
					if (itemTaskCount == 0)
					{
						syncReportsToServer(callback);
					}		
				}
			}
		});
    }
    
    private static void sendCreateReportRequest(final Report report, final SyncDataCallback callback)
    {
    	System.out.println("create report：local id " + report.getLocalID());
    	CreateReportRequest request = new CreateReportRequest(report);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				CreateReportResponse response = new CreateReportResponse(httpResponse);
				if (response.getStatus())
				{
			    	System.out.println("create report：local id " + report.getLocalID() + " *Succeed*");
			    	int currentTime = Utils.getCurrentTime();
					report.setLocalUpdatedDate(currentTime);
					report.setServerUpdatedDate(currentTime);
					report.setServerID(response.getReportID());
					DBManager.getDBManager().updateReportByLocalID(report);
					reportTaskCount--;
					if (reportTaskCount == 0 && callback != null)
					{
						callback.execute();	
					}
				}
				else
				{
			    	System.out.println("create report：local id " + report.getLocalID() + " *Failed*");
					reportTaskCount--;
					if (reportTaskCount == 0 && callback != null)
					{
						callback.execute();	
					}
				}
			}
		});
    }

    private static void sendUpdateReportRequest(final Report report, final SyncDataCallback callback)
    {
    	System.out.println("modify report：local id " + report.getLocalID());
    	ModifyReportRequest request = new ModifyReportRequest(report);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
		    	System.out.println("modify report：local id " + report.getLocalID() + " *Succeed*");
				ModifyReportResponse response = new ModifyReportResponse(httpResponse);
				if (response.getStatus())
				{
					report.setLocalUpdatedDate(Utils.getCurrentTime());
					report.setServerUpdatedDate(report.getLocalUpdatedDate());
					DBManager.getDBManager().updateReportByLocalID(report);
					reportTaskCount--;
					if (reportTaskCount == 0 && callback != null)
					{
						callback.execute();	
					}
				}
				else
				{
			    	System.out.println("modify report：local id " + report.getLocalID() + " *Failed*");
					reportTaskCount--;
					if (reportTaskCount == 0 && callback != null)
					{
						callback.execute();	
					}
				}
			}
		});
    }

	public interface callback
	{
		void execute();
	}	
}
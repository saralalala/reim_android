package netUtils;

import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.List;

import classes.model.Image;
import classes.model.Item;
import classes.model.Report;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import netUtils.request.SyncDataRequest;
import netUtils.request.UploadImageRequest;
import netUtils.request.item.CreateItemRequest;
import netUtils.request.item.ModifyItemRequest;
import netUtils.request.report.CreateReportRequest;
import netUtils.request.report.ModifyReportRequest;
import netUtils.response.SyncDataResponse;
import netUtils.response.UploadImageResponse;
import netUtils.response.item.CreateItemResponse;
import netUtils.response.item.ModifyItemResponse;
import netUtils.response.report.CreateReportResponse;
import netUtils.response.report.ModifyReportResponse;

public abstract class SyncUtils
{
	public static boolean isSyncOnGoing = false;
	private static int imageTaskCount = 0;
	private static int itemTaskCount = 0;
	private static int reportTaskCount = 0;
	
	public static boolean canSyncToServer()
	{
//		AppPreference appPreference = AppPreference.getAppPreference();
//		if (isSyncOnGoing)
//		{
//			return false;
//		}
//		else if (appPreference.syncOnlyWithWifi() && Utils.isWiFiConnected())
//		{
//			return true;
//		}
//		else if (!appPreference.syncOnlyWithWifi() && Utils.isNetworkConnected())
//		{
//			return true;
//		}
//		else
//		{
//			return false;
//		}
        return !isSyncOnGoing && PhoneUtils.isNetworkConnected();
    }
	
	public static void syncFromServer(final SyncDataCallback callback)
	{
//		int lastSynctime = AppPreference.getAppPreference().getLastSyncTime();
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
					for (Item item : response.getItemList())
					{
						Report report = item.getBelongReport();
						report.setLocalID(reportIDArray.get(report.getServerID()));
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
					isSyncOnGoing = false;
				}
			}
		});
	}
	
    public static void syncAllToServer(SyncDataCallback callback)
    {
		System.out.println("------------- syncAllToServer ------------");
    	imageTaskCount = 0;
    	itemTaskCount = 0;
    	reportTaskCount = 0;
    	
    	AppPreference appPreference = AppPreference.getAppPreference();
    	DBManager dbManager = DBManager.getDBManager();
    	List<Item> itemList = dbManager.getUnsyncedItems(appPreference.getCurrentUserID());
    	List<Image> imageList = new ArrayList<Image>();
    	
    	for (Item item : itemList)
		{
			for (Image image : item.getInvoices())
			{
				if (image.isNotUploaded())
				{
					imageList.add(image);
				}
			}
		}
    	
    	imageTaskCount = imageList.size();
    	
    	System.out.println("imageTaskCount："+imageTaskCount);
    	if (imageTaskCount > 0)
		{
    		for (Image image : imageList)
			{
				sendUploadImageRequest(image, callback);
			}		
		}
    	else
		{
    		syncItemsToServer(callback);
		}
    }
    
    private static void syncItemsToServer(SyncDataCallback callback)
    {
    	AppPreference appPreference = AppPreference.getAppPreference();
    	DBManager dbManager = DBManager.getDBManager();
    	List<Item> itemList = dbManager.getUnsyncedItems(appPreference.getCurrentUserID());
    	itemTaskCount = itemList.size();
    	System.out.println("itemTaskCount："+itemTaskCount);
    	if (itemTaskCount > 0)
		{
        	for (Item item : itemList)
    		{
    			if (item.hasUnuploadedInvoice())
    			{
    		    	System.out.println("ignore item：local id " + item.getLocalID());
					itemTaskCount--;
					if (itemTaskCount == 0)
					{
						syncReportsToServer(callback);
					}
    			}
    			else if (item.getServerID() == -1)
    			{
    				sendCreateItemRequest(item, callback);
    			}
    			else
    			{
    				sendModifyItemRequest(item, callback);
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
    			if (report.getServerID() == -1)
    			{
    				sendCreateReportRequest(report, callback);
    			}
    			else
    			{
    				sendModifyReportRequest(report, callback);
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
    
    private static void sendUploadImageRequest(final Image image, final SyncDataCallback callback)
    {
    	System.out.println("upload image：local id " + image.getLocalID());
		UploadImageRequest request = new UploadImageRequest(image.getLocalPath(), NetworkConstant.IMAGE_TYPE_INVOICE);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final UploadImageResponse response = new UploadImageResponse(httpResponse);
				if (response.getStatus())
				{
			    	System.out.println("upload image：local id " + image.getLocalID() + " *Succeed*");
					image.setServerID(response.getImageID());
                    image.setServerPath(response.getPath());
					DBManager.getDBManager().updateImageServerID(image);
				}
				else
				{
			    	System.out.println("upload image：local id " + image.getLocalID() + " *Failed*");
				}
				
				imageTaskCount--;
				if (imageTaskCount == 0)
				{
					syncItemsToServer(callback);
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
					
					DBManager.getDBManager().updateItemByLocalID(item);
				}
				else
				{
			    	System.out.println("create item：local id " + item.getLocalID() + " *Failed*");
				}
				
				itemTaskCount--;
				if (itemTaskCount == 0)
				{
					syncReportsToServer(callback);
				}
			}
		});
    }

    private static void sendModifyItemRequest(final Item item, final SyncDataCallback callback)
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
				}
				else
				{
			    	System.out.println("modify item：local id " + item.getLocalID() + " *Failed*");
				}
				
				itemTaskCount--;
				if (itemTaskCount == 0)
				{
					syncReportsToServer(callback);
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
				}
				else
				{
			    	System.out.println("create report：local id " + report.getLocalID() + " *Failed*");
				}
				
				reportTaskCount--;
				if (reportTaskCount == 0 && callback != null)
				{
					callback.execute();	
				}
			}
		});
    }

    private static void sendModifyReportRequest(final Report report, final SyncDataCallback callback)
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
				}
				else
				{
			    	System.out.println("modify report：local id " + report.getLocalID() + " *Failed*");
				}

				reportTaskCount--;
				if (reportTaskCount == 0 && callback != null)
				{
					callback.execute();	
				}
			}
		});
    }
}
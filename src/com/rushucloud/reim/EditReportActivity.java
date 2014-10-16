package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConstant;
import netUtils.Request.UploadImageRequest;
import netUtils.HttpConnectionCallback;
import netUtils.Request.Item.CreateItemRequest;
import netUtils.Request.Report.CreateReportRequest;
import netUtils.Request.Report.ModifyReportRequest;
import netUtils.Response.UploadImageResponse;
import netUtils.Response.Item.CreateItemResponse;
import netUtils.Response.Report.CreateReportResponse;
import netUtils.Response.Report.ModifyReportResponse;

import classes.AppPreference;
import classes.Item;
import classes.Report;
import classes.Utils;
import classes.Adapter.ItemListViewAdapter;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class EditReportActivity extends Activity
{
	private AppPreference appPreference;
	private DBManager dbManager;
	
	private EditText titleEditText;
	private ListView itemListView;
	private ItemListViewAdapter adapter;
	
	private Report report;
	private List<Item> itemList = null;
	private ArrayList<Integer> chosenItemIDList = null;
	private ArrayList<Integer> remainingItemIDList = null;
	
	private boolean needToSave = true;
	private boolean needToSubmit = false;
	
	private static int taskCount;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_edit);
		dataInitialise();
		viewInitialise();
		buttonInitialise();
	}
	
	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EditReportActivity");		
		MobclickAgent.onResume(this);
		refreshListView();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("EditReportActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean onCreateOptionsMenu(Menu menu)
	{
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
		{
			return false;
		}
		else
		{
			getMenuInflater().inflate(R.menu.single_item, menu);
			MenuItem item = menu.getItem(0);
			item.setTitle(getResources().getString(R.string.submit));
			return true;			
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.action_item)
		{
			needToSubmit = true;
			saveReport();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("选项");
		menu.add(0, 0, 0, "删除");
	}
	
	public boolean onContextItemSelected(MenuItem item)
	{
    	AdapterContextMenuInfo menuInfo=(AdapterContextMenuInfo)item.getMenuInfo();
    	final int index = (int)itemListView.getAdapter().getItemId(menuInfo.position);
    	switch (item.getItemId()) 
    	{
			case 0:
				int id = chosenItemIDList.remove(index);
				remainingItemIDList.add(id);
				itemList.remove(index);
				adapter.set(itemList);
				adapter.notifyDataSetChanged();
				break;
			default:
				break;
		}    		
		
		return super.onContextItemSelected(item);
	}
	
	private void dataInitialise()
	{
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null)
		{
			// new report from ReportFragment
			report = new Report();
			report.setStatus(Report.STATUS_DRAFT);
			report.setUser(dbManager.getUser(appPreference.getCurrentUserID()));
			chosenItemIDList = new ArrayList<Integer>();
			remainingItemIDList = Utils.itemListToIDArray(dbManager.getUnarchivedUserItems(appPreference.getCurrentUserID()));
			itemList = new ArrayList<Item>();
		}
		else
		{
			report = (Report)bundle.getSerializable("report");
			chosenItemIDList = bundle.getIntegerArrayList("chosenItemIDList");
			if (chosenItemIDList == null)
			{
				// edit report from ReportFragment
				itemList = dbManager.getReportItems(report.getLocalID());
				chosenItemIDList = Utils.itemListToIDArray(itemList);
				remainingItemIDList = Utils.itemListToIDArray(dbManager.getUnarchivedUserItems(appPreference.getCurrentUserID()));
			}
			else
			{
				// edit report from UnarchivedActivity
				remainingItemIDList = bundle.getIntegerArrayList("remainingItemIDList");	
				itemList = dbManager.getItems(chosenItemIDList);
			}
		}
	}
	
	private void viewInitialise()
	{
		titleEditText = (EditText)findViewById(R.id.titleEditText);
		titleEditText.setText(report.getTitle());
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
		{
			titleEditText.setFocusable(false);
		}
		
		adapter = new ItemListViewAdapter(EditReportActivity.this, itemList);
		itemListView = (ListView)findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
				{
					Intent intent = new Intent(EditReportActivity.this, ShowItemActivity.class);
					intent.putExtra("itemLocalID", itemList.get(position).getLocalID());
					startActivity(intent);	
				}
				else
				{
					Intent intent = new Intent(EditReportActivity.this, EditItemActivity.class);
					intent.putExtra("itemLocalID", itemList.get(position).getLocalID());
					startActivity(intent);					
				}
			}
		});
		if (report.getStatus() == Report.STATUS_DRAFT || report.getStatus() == Report.STATUS_REJECT)
		{
			registerForContextMenu(itemListView);			
		}
	}
	
	private void buttonInitialise()
	{
		Button addButton = (Button)findViewById(R.id.addButton);
		addButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				if (remainingItemIDList.size() == 0)
				{
					AlertDialog alertDialog = new AlertDialog.Builder(EditReportActivity.this)
												.setTitle("提示")
												.setMessage("所有条目均已被添加到各个报告中")
												.setNegativeButton(R.string.confirm, null)
												.create();
					alertDialog.show();
				}
				else
				{
					report.setTitle(titleEditText.getText().toString());
					
					Bundle bundle = new Bundle();
					bundle.putSerializable("report", report);
					bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
					bundle.putIntegerArrayList("remainingItemIDList", remainingItemIDList);
					Intent intent = new Intent(EditReportActivity.this, UnarchivedItemsActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();					
				}
			}
		});
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
		{
			addButton.setVisibility(View.GONE);
		}
		
		Button saveButton = (Button)findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					if (report.getStatus() == 0 || report.getStatus() == 4)
					{
						report.setLocalUpdatedDate(Utils.getCurrentTime());
						report.setTitle(titleEditText.getText().toString());
						if (report.getLocalID() == -1)
						{
							report.setCreatedDate(Utils.getCurrentTime());
							dbManager.insertReport(report);
							report.setLocalID(dbManager.getLastInsertReportID());								
						}
						else
						{
							dbManager.updateReportByLocalID(report);
						}
						if (dbManager.updateReportItems(chosenItemIDList, report.getLocalID()))
						{
							saveReport();
						}
						else
						{
							AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
																.setTitle("保存失败")
																.setNegativeButton(R.string.confirm, null)
																.create();
							mDialog.show();
						}
					}
					else
					{
						finish();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
		{
			saveButton.setEnabled(false);
		}
		
		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
	}
	
	private void refreshListView()
	{
		adapter.set(itemList);
		adapter.notifyDataSetChanged();
	}	
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
    }

    private void saveReport()
    {
    	if (itemList == null || itemList.size() == 0)
		{
			if (report.getServerID() == -1)
			{
				sendCreateReportRequest();
			}
			else
			{
				sendUpdateReportRequest();
			}
		}    	

    	taskCount = 0;
    	boolean flag = false;
    	for (Item item : itemList)
		{
			if (item.getServerID() == -1 && !item.getInvoicePath().equals(""))
			{
				flag = true;
				taskCount++;
				sendUploadImageRequest(item);
			}
			else if (item.getServerID() == -1)
			{
				flag = true;
				taskCount++;
				sendCreateItemRequest(item);
			}
		}
    	if (!flag)
		{
			if (report.getServerID() == -1)
			{
				sendCreateReportRequest();
			}
			else
			{
				sendUpdateReportRequest();
			}
		}
    }
    
    private void submitReport()
    {
    	ModifyReportRequest request = new ModifyReportRequest(report);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ModifyReportResponse response = new ModifyReportResponse(httpResponse);
				if (response.getStatus())
				{
					dbManager.updateReportByLocalID(report);
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
													.setTitle("报告已提交！请等待审批！")
													.setNegativeButton(R.string.confirm, 
															new DialogInterface.OnClickListener()
													{
														public void onClick(DialogInterface dialog, int which)
														{
															finish();
														}
													})
													.create();
							mDialog.show();
						}
					});		
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
													.setTitle("报告提交失败！")
													.setMessage(response.getErrorMessage())
													.setNegativeButton(R.string.confirm, null)
													.create();
							mDialog.show();
						}
					});		
				}
			}
		});
    }

    private void sendCreateReportRequest()
    {
    	CreateReportRequest request = new CreateReportRequest(report);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				CreateReportResponse response = new CreateReportResponse(httpResponse);
				if (response.getStatus())
				{
					report.setServerID(response.getReportID());
					dbManager.updateReportByLocalID(report);
					if (needToSubmit)
					{
						report.setStatus(Report.STATUS_SUBMITTED);
						submitReport();
					}
					else
					{
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								Toast.makeText(EditReportActivity.this, "报告上传成功", Toast.LENGTH_SHORT).show();
							}
						});						
					}
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							Toast.makeText(EditReportActivity.this, "报告上传失败", Toast.LENGTH_SHORT).show();
						}
					});		
				}
			}
		});
    }

    private void sendUpdateReportRequest()
    {
    	ModifyReportRequest request = new ModifyReportRequest(report);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				ModifyReportResponse response = new ModifyReportResponse(httpResponse);
				if (response.getStatus())
				{
					dbManager.updateReportByLocalID(report);
					if (needToSubmit)
					{
						report.setStatus(Report.STATUS_SUBMITTED);
						submitReport();
					}
					else
					{
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								Toast.makeText(EditReportActivity.this, "报告更新成功", Toast.LENGTH_SHORT).show();
							}
						});						
					}
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							Toast.makeText(EditReportActivity.this, "报告更新失败", Toast.LENGTH_SHORT).show();	
						}
					});					
				}
			}
		});
    }
    
    private void sendUploadImageRequest(final Item item)
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
					dbManager.updateItemByLocalID(item);
					sendCreateItemRequest(item);
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							taskCount--;
							needToSave = false;
							Toast.makeText(EditReportActivity.this, "图片上传失败", Toast.LENGTH_SHORT).show();
						}
					});				
				}
			}
		});
    }
    
    private void sendCreateItemRequest(final Item item)
    {
		CreateItemRequest request = new CreateItemRequest(item);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final CreateItemResponse response = new CreateItemResponse(httpResponse);
				if (response.getStatus())
				{
					item.setServerID(response.getItemID());
					dbManager.updateItemByLocalID(item);
					taskCount--;
					if (needToSave && taskCount == 0)
					{
						if (report.getServerID() == -1)
						{
							sendCreateReportRequest();
						}
						else
						{
							sendUpdateReportRequest();
						}
					}
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							taskCount--;
							needToSave = false;
							Toast.makeText(EditReportActivity.this, "图片上传失败", Toast.LENGTH_SHORT).show();					
						}
					});				
				}
			}
		});
    }
}
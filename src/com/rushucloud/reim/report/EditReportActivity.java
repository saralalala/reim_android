package com.rushucloud.reim.report;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.SyncDataCallback;
import netUtils.SyncUtils;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.Report.CreateReportResponse;
import netUtils.Response.Report.GetReportResponse;
import netUtils.Response.Report.ModifyReportResponse;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Report.CreateReportRequest;
import netUtils.Request.Report.GetReportRequest;
import netUtils.Request.Report.ModifyReportRequest;
import classes.Category;
import classes.Comment;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.User;
import classes.adapter.MemberListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.TextLengthFilter;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

import com.rushucloud.reim.R;
import com.rushucloud.reim.item.EditItemActivity;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class EditReportActivity extends Activity
{
	private AppPreference appPreference;
	private DBManager dbManager;
	
	private EditText titleEditText;
	private TextView timeTextView;
	private TextView statusTextView;
	private TextView managerTextView;
	private ListView managerListView;
	private PopupWindow managerPopupWindow;
	private TextView ccTextView;
	private ListView ccListView;
	private PopupWindow ccPopupWindow;
	private TextView amountTextView;
	private TextView itemCountTextView;
	private LinearLayout itemLayout;
	private MemberListViewAdapter memberAdapter;
	private PopupWindow deletePopupWindow;

	private Report report;
	private List<Item> itemList = null;
	private ArrayList<Integer> chosenItemIDList = null;
	
	private List<User> userList;
	private User currentUser;
	private boolean[] managerCheckList;
	private boolean[] ccCheckList;
	
	private int itemIndex;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_edit);
		initData();
		initView();
	}
	
	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EditReportActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
		refreshView();
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
	
	private void initData()
	{
		if (appPreference == null)
		{
			appPreference = AppPreference.getAppPreference();
		}
		if (dbManager == null)
		{
			dbManager = DBManager.getDBManager();
		}
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null)
		{
			// new report from ReportFragment
			report = new Report();
			report.setSender(appPreference.getCurrentUser());
			chosenItemIDList = new ArrayList<Integer>();
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
				chosenItemIDList = Item.getItemsIDArray(itemList);
			}
			else
			{
				// edit report from UnarchivedActivity
				itemList = dbManager.getItems(chosenItemIDList);
			}
		}

    	currentUser = appPreference.getCurrentUser();
    	
    	int currentGroupID = appPreference.getCurrentGroupID();
		userList = User.removeCurrentUserFromList(dbManager.getGroupUsers(currentGroupID));

		if (report.getManagerList() == null || report.getManagerList().isEmpty())
		{
			managerCheckList = User.getUsersCheck(userList, currentUser.constructListWithManager());
		}
		else
		{
			managerCheckList = User.getUsersCheck(userList, report.getManagerList());
		}
		
    	ccCheckList = User.getUsersCheck(userList, report.getCCList());
	}
	
	private void initView()
	{
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}	
		});
		
		TextView saveTextView = (TextView)findViewById(R.id.saveTextView);
		saveTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				saveReport();
			}
		});
		
		itemLayout = (LinearLayout) findViewById(R.id.itemLayout);
		
		titleEditText = (EditText) findViewById(R.id.titleEditText);
		titleEditText.setOnFocusChangeListener(ViewUtils.getEditTextFocusChangeListener());
		InputFilter[] filters = { new TextLengthFilter(10) };
		titleEditText.setFilters(filters);
		
		timeTextView = (TextView) findViewById(R.id.timeTextView);
		statusTextView = (TextView) findViewById(R.id.statusTextView);
		
		TextView approveInfoTextView = (TextView)findViewById(R.id.approveInfoTextView);
		approveInfoTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(EditReportActivity.this, ApproveInfoActivity.class));
			}
		});
		
		managerTextView = (TextView) findViewById(R.id.managerTextView);
		managerTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				showManagerWindow();
			}
		});
		initManagerView();
		
		ccTextView = (TextView) findViewById(R.id.ccTextView);
		ccTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				showCCWindow();
			}
		});
		initCCView();
		
		amountTextView = (TextView) findViewById(R.id.amountTextView);
		amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
		itemCountTextView = (TextView) findViewById(R.id.itemCountTextView);
		
		ImageView addImageView = (ImageView) findViewById(R.id.addImageView);
		addImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				report.setTitle(titleEditText.getText().toString());
				
				Bundle bundle = new Bundle();
				bundle.putSerializable("report", report);
				bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
				Intent intent = new Intent(EditReportActivity.this, UnarchivedItemsActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			}
		});
		
		Button submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(EditReportActivity.this, "UMENG_POST_REPORT_DETAIL");
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(EditReportActivity.this, R.string.error_submit_network_unavailable);
				}
				else if (report.getManagerList() == null || report.getManagerList().isEmpty())
				{
					ViewUtils.showToast(EditReportActivity.this, R.string.no_manager);
				}
				else
				{
					submitReport();
				}
			}
		});
		
		Button commentButton = (Button)findViewById(R.id.commentButton);
		commentButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (report.getCommentList() == null || report.getCommentList().isEmpty())
				{
					if (!PhoneUtils.isNetworkConnected())
					{
						ViewUtils.showToast(EditReportActivity.this, R.string.error_comment_network_unavailable);
					}
					else
					{
						showAddCommentDialog();
					}					
				}
				else
				{
					Bundle bundle = new Bundle();
					bundle.putString("source", "EditReportActivity");
					bundle.putInt("reportLocalID", report.getLocalID());
					Intent intent = new Intent(EditReportActivity.this, CommentActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);					
				}
			}
		});	
	
		initDeleteWindow();
	}
	
	private void initManagerView()
	{
    	View managerView = View.inflate(this, R.layout.window_report_manager, null);
    	managerListView = (ListView) managerView.findViewById(R.id.userListView);
    	managerListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				managerCheckList[position] = !managerCheckList[position];
				memberAdapter.setCheck(managerCheckList);
				memberAdapter.notifyDataSetChanged();
			}
		});

		ImageView backImageView = (ImageView) managerView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				managerPopupWindow.dismiss();
			}
		});
		
		TextView confirmTextView = (TextView) managerView.findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				managerPopupWindow.dismiss();
				
				List<User> managerList = new ArrayList<User>();
				if (managerCheckList != null)
				{
					for (int i = 0; i < managerCheckList.length; i++)
					{
						if (managerCheckList[i])
						{
							managerList.add(userList.get(i));
						}
					}
				}

				report.setManagerList(managerList);
				managerTextView.setText(report.getManagersName());
			}
		});

		managerPopupWindow = ViewUtils.constructHorizontalPopupWindow(this, managerView);	
	}
	
	private void initCCView()
	{
    	View ccView = View.inflate(this, R.layout.window_report_cc, null);
    	ccListView = (ListView) ccView.findViewById(R.id.userListView);
    	ccListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				ccCheckList[position] = !ccCheckList[position];
				memberAdapter.setCheck(ccCheckList);
				memberAdapter.notifyDataSetChanged();
			}
		});

		ImageView backImageView = (ImageView) ccView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				ccPopupWindow.dismiss();
			}
		});
		
		TextView confirmTextView = (TextView) ccView.findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				ccPopupWindow.dismiss();

				List<User> ccList = new ArrayList<User>();
				if (ccCheckList != null)
				{
					for (int i = 0; i < ccCheckList.length; i++)
					{
						if (ccCheckList[i])
						{
							ccList.add(userList.get(i));
						}
					}
				}
				
				report.setCCList(ccList);
				ccTextView.setText(report.getCCsName());
			}
		});

		ccPopupWindow = ViewUtils.constructHorizontalPopupWindow(this, ccView);	
	}
	
	private void initDeleteWindow()
	{
		View deleteView = View.inflate(this, R.layout.window_delete, null);
		
		Button deleteButton = (Button) deleteView.findViewById(R.id.deleteButton);
		deleteButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				chosenItemIDList.remove(itemIndex);
				itemList.remove(itemIndex);
				
				deletePopupWindow.dismiss();
				refreshView();
			}
		});
		deleteButton = ViewUtils.resizeWindowButton(deleteButton);
		
		Button cancelButton = (Button) deleteView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				deletePopupWindow.dismiss();
			}
		});
		cancelButton = ViewUtils.resizeWindowButton(cancelButton);
		
		deletePopupWindow = ViewUtils.constructBottomPopupWindow(this, deleteView);		
	}
	
	private void refreshView()
	{
		itemList = dbManager.getItems(Item.getItemsIDArray(itemList));
		
		titleEditText.setText(report.getTitle());
		if (report.getTitle().equals(""))
		{
			titleEditText.requestFocus();
		}
		
		String createDate = report.getCreatedDate() == -1 ? getString(R.string.not_available) : Utils.secondToStringUpToMinute(report.getCreatedDate());
		timeTextView.setText(createDate);
		
		statusTextView.setText(report.getStatusString());
		statusTextView.setBackgroundResource(report.getStatusBackground());
		
		managerTextView.setText(report.getManagersName());		
		ccTextView.setText(report.getCCsName());
		
		int itemCount = itemList.size();
		itemCountTextView.setText(itemCount + getString(R.string.item_count));

		itemLayout.removeAllViews();
		
		double amount = 0;
		for (int i = 0; i < itemList.size(); i++)
		{
			LayoutInflater inflater = LayoutInflater.from(this);
			final Item item = itemList.get(i);
			final int index = i;
			View view = inflater.inflate(R.layout.list_report_item_edit, null);
			view.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					Intent intent = new Intent(EditReportActivity.this, EditItemActivity.class);
					intent.putExtra("itemLocalID", item.getLocalID());
					startActivity(intent);
				}
			});
			view.setOnLongClickListener(new OnLongClickListener()
			{
				public boolean onLongClick(View v)
				{
					itemIndex = index;
					showDeleteWindow();
					return false;
				}
			});
			
			TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
			TextView vendorTextView = (TextView) view.findViewById(R.id.vendorTextView);
			ImageView categoryImageView = (ImageView) view.findViewById(R.id.categoryImageView);
			ImageView warningImageView = (ImageView) view.findViewById(R.id.warningImageView);
			
			amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
			amountTextView.setText(Utils.formatDouble(item.getAmount()));

			String vendor = item.getVendor().equals("") ? getString(R.string.vendor_not_available) : item.getVendor();
			vendorTextView.setText(vendor);
			
			if (item.missingInfo())
			{
				warningImageView.setVisibility(View.VISIBLE);
			}
			else
			{
				Category category = item.getCategory();
				Bitmap bitmap = BitmapFactory.decodeFile(category.getIconPath());
				if (bitmap != null)
				{
					categoryImageView.setImageBitmap(bitmap);				
				}
			}
			
			itemLayout.addView(view);

			amount += item.getAmount();
		}
		amountTextView.setText(Utils.formatDouble(amount));
		
		if (report.getServerID() != -1 && PhoneUtils.isNetworkConnected())
		{
			sendGetReportRequest(report.getServerID());
		}
	}	
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
    }

    private void showDeleteWindow()
    {    	
		deletePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		deletePopupWindow.update();
		
		ViewUtils.dimBackground(this);
    }
    
    private void showAddCommentDialog()
    {
		View view = View.inflate(this, R.layout.dialog_report_comment, null);
		
		TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
		titleTextView.setText(R.string.add_comment);
		
		final EditText commentEditText = (EditText)view.findViewById(R.id.commentEditText);
		commentEditText.setOnFocusChangeListener(ViewUtils.getEditTextFocusChangeListener());
		commentEditText.requestFocus();
		
    	Builder builder = new Builder(this);
    	builder.setView(view);
    	builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										String comment = commentEditText.getText().toString();
										if (comment.equals(""))
										{
											ViewUtils.showToast(EditReportActivity.this, R.string.error_comment_empty);
										}
										else
										{
											if (report.getServerID() == -1)
											{
												sendCreateReportRequest(comment);
											}
											else
											{
												sendModifyReportRequest(comment);
											}
										}
									}
								});
    	builder.setNegativeButton(R.string.cancel, null);
    	builder.create().show();
    }
    
    private void showManagerWindow()
    {
    	hideSoftKeyboard();

    	if (memberAdapter == null)
		{
			memberAdapter = new MemberListViewAdapter(this, userList, managerCheckList);
		}
    	else
    	{
        	memberAdapter.setCheck(managerCheckList);			
		}
    	managerListView.setAdapter(memberAdapter);
    	
    	managerPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
    	managerPopupWindow.update();

    	downloadAvatars();
    }

    private void showCCWindow()
    {
    	hideSoftKeyboard();

    	if (memberAdapter == null)
		{
			memberAdapter = new MemberListViewAdapter(this, userList, ccCheckList);
		}
    	else
    	{
        	memberAdapter.setCheck(ccCheckList);		
		}
    	ccListView.setAdapter(memberAdapter);
    	
    	ccPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
    	ccPopupWindow.update();
    	
    	downloadAvatars();
    }

    private void saveReport()
    {
    	hideSoftKeyboard();
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
			if (SyncUtils.canSyncToServer())
			{
				SyncUtils.isSyncOnGoing = true;
				SyncUtils.syncAllToServer(new SyncDataCallback()
				{
					public void execute()
					{
						SyncUtils.isSyncOnGoing = false;
					}
				});
			}
			ViewUtils.showToast(EditReportActivity.this, R.string.succeed_in_saving_report);
			finish();
		}
		else
		{
			ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_save_report);
		}
    }

    private void submitReport()
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
			if (!report.hasItems())
			{
				if (report.getStatus() != Report.STATUS_REJECTED)
				{
					report.setStatus(Report.STATUS_DRAFT);					
				}
				ViewUtils.showToast(this, R.string.error_submit_report_empty);
			}
			else if (appPreference.getCurrentGroupID() == -1)
			{
				report.setStatus(Report.STATUS_FINISHED);
				ViewUtils.showToast(this, R.string.succeed_in_submitting_report);
				finish();
			}
			else
			{
				report.setStatus(Report.STATUS_SUBMITTED);
				ViewUtils.showToast(this, R.string.succeed_in_submitting_report);
				finish();
			}
			dbManager.updateReportByLocalID(report);
			if (SyncUtils.canSyncToServer())
			{
				SyncUtils.isSyncOnGoing = true;
				SyncUtils.syncAllToServer(new SyncDataCallback()
				{
					public void execute()
					{
						SyncUtils.isSyncOnGoing = false;
					}
				});
			}
		}
		else
		{
			ViewUtils.showToast(this, R.string.failed_to_save_report);
		}
    }

    private void downloadAvatars()
    {
    	if (PhoneUtils.isNetworkConnected())
		{
        	for (User user : userList)
    		{
    			if (user.hasUndownloadedAvatar())
    			{
    				sendDownloadAvatarRequest(user);
    			}	
    		}			
		}    	
    }
    
    private void sendDownloadAvatarRequest(final User user)
    {
    	final DBManager dbManager = DBManager.getDBManager();
    	DownloadImageRequest request = new DownloadImageRequest(user.getAvatarID(), DownloadImageRequest.IMAGE_QUALITY_VERY_HIGH);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					String avatarPath = PhoneUtils.saveBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_AVATAR);
					user.setAvatarPath(avatarPath);
					user.setLocalUpdatedDate(Utils.getCurrentTime());
					user.setServerUpdatedDate(user.getLocalUpdatedDate());
					dbManager.updateUser(user);
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							List<User> memberList = dbManager.getGroupUsers(appPreference.getCurrentGroupID());
							memberAdapter.setMember(User.removeCurrentUserFromList(memberList));
							memberAdapter.notifyDataSetChanged();
						}
					});	
				}
			}
		});
    }
	
    private void sendGetReportRequest(final int reportServerID)
    {
		ReimProgressDialog.show();
    	GetReportRequest request = new GetReportRequest(reportServerID);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final GetReportResponse response = new GetReportResponse(httpResponse);
				if (response.getStatus())
				{
					if (report.getLocalUpdatedDate() <= response.getReport().getServerUpdatedDate())
					{
						report.setManagerList(response.getReport().getManagerList());
						report.setCCList(response.getReport().getCCList());
						report.setCommentList(response.getReport().getCommentList());
						dbManager.updateReportByLocalID(report);
						
						dbManager.deleteReportComments(report.getLocalID());
						for (Comment comment : report.getCommentList())
						{
							comment.setReportID(report.getLocalID());
							dbManager.insertComment(comment);
						}
					}
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							managerTextView.setText(report.getManagersName());		
							ccTextView.setText(report.getCCsName());
						}
					});
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_get_data);
						}
					});
				}
			}
		});
    }
    
    private void sendCreateReportRequest(final String commentContent)
    {
		ReimProgressDialog.show();

		report.setTitle(titleEditText.getText().toString());
		report.setCreatedDate(Utils.getCurrentTime());
		report.setLocalUpdatedDate(report.getCreatedDate());
		dbManager.insertReport(report);
		report.setLocalID(dbManager.getLastInsertReportID());
		dbManager.updateReportItems(chosenItemIDList, report.getLocalID());
    	
    	CreateReportRequest request = new CreateReportRequest(report, commentContent);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final CreateReportResponse response = new CreateReportResponse(httpResponse);
				if (response.getStatus())
				{
					int currentTime = Utils.getCurrentTime();
					
					report.setServerID(response.getReportID());
					report.setServerUpdatedDate(currentTime);
					report.setLocalUpdatedDate(currentTime);
					dbManager.updateReportByLocalID(report);
					
					Comment comment = new Comment();
					comment.setContent(commentContent);
					comment.setCreatedDate(currentTime);
					comment.setLocalUpdatedDate(currentTime);
					comment.setServerUpdatedDate(currentTime);
					comment.setReportID(report.getLocalID());
					comment.setReviewer(currentUser);
					dbManager.insertComment(comment);					
					
					report.setCommentList(dbManager.getReportComments(report.getLocalID()));
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditReportActivity.this, R.string.succeed_in_sending_comment);
						}
					});
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_send_comment, response.getErrorMessage());
						}
					});					
				}
			}
		});
    }
    
    private void sendModifyReportRequest(final String commentContent)
    {
		ReimProgressDialog.show();
    	
		report.setTitle(titleEditText.getText().toString());
		dbManager.updateReportByLocalID(report);
		dbManager.updateReportItems(chosenItemIDList, report.getLocalID());
		
    	ModifyReportRequest request = new ModifyReportRequest(report, commentContent);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ModifyReportResponse response = new ModifyReportResponse(httpResponse);
				if (response.getStatus())
				{
					int currentTime = Utils.getCurrentTime();
					
					report.setServerUpdatedDate(currentTime);
					report.setLocalUpdatedDate(currentTime);
					dbManager.updateReportByLocalID(report);
					
					Comment comment = new Comment();
					comment.setContent(commentContent);
					comment.setCreatedDate(currentTime);
					comment.setLocalUpdatedDate(currentTime);
					comment.setServerUpdatedDate(currentTime);
					comment.setReportID(report.getLocalID());
					comment.setReviewer(currentUser);
					dbManager.insertComment(comment);

					report.setCommentList(dbManager.getReportComments(report.getLocalID()));
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditReportActivity.this, R.string.succeed_in_sending_comment);
						}
					});
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_send_comment, response.getErrorMessage());
						}
					});					
				}
			}
		});
    }
}
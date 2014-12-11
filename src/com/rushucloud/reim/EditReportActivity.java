package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.SyncDataCallback;
import netUtils.SyncUtils;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Report.CreateReportRequest;
import netUtils.Request.Report.GetReportRequest;
import netUtils.Request.Report.ModifyReportRequest;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.Report.CreateReportResponse;
import netUtils.Response.Report.GetReportResponse;
import netUtils.Response.Report.ModifyReportResponse;
import classes.AppPreference;
import classes.Category;
import classes.Comment;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.User;
import classes.Utils;
import classes.Adapter.MemberListViewAdapter;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
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
	private ImageView statusImageView;
	private TextView managerTextView;
	private TextView ccTextView;
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
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_edit);
		initView();
	}
	
	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EditReportActivity");		
		MobclickAgent.onResume(this);
		initData();
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
				saveReport("报告保存成功");
			}
		});
		
		itemLayout = (LinearLayout) findViewById(R.id.itemLayout);
		
		titleEditText = (EditText) findViewById(R.id.titleEditText);
		timeTextView = (TextView) findViewById(R.id.timeTextView);
		statusImageView = (ImageView) findViewById(R.id.statusImageView);
		
		managerTextView = (TextView) findViewById(R.id.managerTextView);
		managerTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				showManagerDialog();
			}
		});
		ccTextView = (TextView) findViewById(R.id.ccTextView);
		ccTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				showCCDialog();
			}
		});
		
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
		
		TextView submitTextView = (TextView) findViewById(R.id.submitTextView);
		submitTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(EditReportActivity.this, "UMENG_POST_REPORT_DETAIL");
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(EditReportActivity.this, "网络未连接，无法提交");
				}
				else if (report.getManagerList() == null || report.getManagerList().size() == 0)
				{
					Utils.showToast(EditReportActivity.this, "未选择汇报对象");
				}
				else
				{
					submitReport();
				}
			}
		});
		
		Button addCommentButton = (Button)findViewById(R.id.addCommentButton);
		addCommentButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(EditReportActivity.this, "网络未连接，无法添加评论");
				}
				else
				{
					showAddCommentDialog();
				}
			}
		});

		Button checkCommentButton = (Button)findViewById(R.id.checkCommentButton);
		checkCommentButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(EditReportActivity.this, CommentActivity.class);
				intent.putExtra("reportLocalID", report.getLocalID());
				startActivity(intent);
			}
		});		
	}
	
	private void refreshView()
	{
		ReimApplication.setProgressDialog(this);	
		
		titleEditText.setText(report.getTitle());
		if (report.getTitle().equals(""))
		{
			titleEditText.requestFocus();
		}
		
		String createDate = report.getCreatedDate() == -1 ? getString(R.string.not_available) : Utils.secondToStringUpToMinute(report.getCreatedDate());
		timeTextView.setText(createDate);
		
		statusImageView.setImageResource(report.getStatusBackground());
		
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
			final int itemIndex = i;
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
					showDeleteDialog(itemIndex);
					return false;
				}
			});
			
			TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
			TextView vendorTextView = (TextView) view.findViewById(R.id.vendorTextView);
			LinearLayout iconLayout = (LinearLayout) view.findViewById(R.id.iconLayout);
			ImageView categoryImageView = (ImageView) view.findViewById(R.id.categoryImageView);
			ImageView warningImageView = (ImageView) view.findViewById(R.id.warningImageView);
			
			amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
			amountTextView.setText(Utils.formatDouble(item.getAmount()));

			String vendor = item.getMerchant().equals("") ? getString(R.string.not_available) : item.getMerchant();
			vendorTextView.setText(vendor);
			
			// category 和 tag 一共iconCount个
			Category category = item.getCategory();
			if (category == null)
			{
				warningImageView.setVisibility(View.VISIBLE);
			}
			else
			{
				Bitmap bitmap = BitmapFactory.decodeFile(category.getIconPath());
				if (bitmap != null)
				{
					categoryImageView.setImageBitmap(bitmap);				
				}					
			}
			
			iconLayout.removeAllViews();
			
			// category 和 tag 一共iconCount个

//			DisplayMetrics metrics = getResources().getDisplayMetrics();
//			int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, metrics);
//			int screenWidth = metrics.widthPixels;
//			int interval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, metrics);
//			int sideLength = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
			
//			int iconCount = (screenWidth - amountTextView.getMeasuredWidth() - padding * 3 + interval) / (sideLength + interval);
//			iconCount = 1;
//			for (int i = 0; i < iconCount; i++)
//			{
//				ImageView iconImageView = new ImageView(this);
//				iconImageView.setImageResource(R.drawable.food);
//				LayoutParams params = new LayoutParams(sideLength, sideLength);
//				params.rightMargin = interval;
//				iconLayout.addView(iconImageView, params);
//			}

			iconLayout.addView(categoryImageView);
			
			itemLayout.addView(view);

			amount += item.getAmount();
		}
		amountTextView.setText(Utils.formatDouble(amount));
		
		if (report.getServerID() != -1 && Utils.isNetworkConnected())
		{
			sendGetReportRequest(report.getServerID());
		}
	}	
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
    }

    private void showDeleteDialog(final int itemIndex)
    {	
    	if (deletePopupWindow == null)
		{
    		final View deleteView = View.inflate(this, R.layout.window_delete, null); 

    		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.window_button_unselected);
    		final double ratio = ((double)bitmap.getHeight()) / bitmap.getWidth();
    		
    		final Button deleteButton = (Button) deleteView.findViewById(R.id.deleteButton);
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
    		deleteButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
    		{
    			public void onGlobalLayout()
    			{
    				ViewGroup.LayoutParams params = deleteButton.getLayoutParams();
    				params.height = (int)(deleteButton.getWidth() * ratio);;
    				deleteButton.setLayoutParams(params);
    			}
    		});
    		
    		final Button cancelButton = (Button) deleteView.findViewById(R.id.cancelButton);
    		cancelButton.setOnClickListener(new View.OnClickListener()
    		{
    			public void onClick(View v)
    			{
    				deletePopupWindow.dismiss();
    			}
    		});
    		cancelButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
    		{
    			public void onGlobalLayout()
    			{
    				ViewGroup.LayoutParams params = cancelButton.getLayoutParams();
    				params.height = (int)(cancelButton.getWidth() * ratio);;
    				cancelButton.setLayoutParams(params);
    			}
    		});
    		
    		deletePopupWindow = Utils.constructPopupWindow(this, deleteView);
		}
    	
		deletePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		deletePopupWindow.update();
		
		Utils.dimBackground(this);
    }
    
    private void showAddCommentDialog()
    {
		View view = View.inflate(this, R.layout.report_comment_dialog, null);
		final EditText commentEditText = (EditText)view.findViewById(R.id.commentEditText);
		commentEditText.requestFocus();
		
    	AlertDialog mDialog = new AlertDialog.Builder(this)
								.setTitle("添加评论")
								.setView(view)
								.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										String comment = commentEditText.getText().toString();
										if (comment.equals(""))
										{
											Utils.showToast(EditReportActivity.this, "评论不能为空");
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
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
		mDialog.show();
    }
    
    private void showManagerDialog()
    {
    	hideSoftKeyboard();
		if (report.getManagerList() == null || report.getManagerList().size() == 0)
		{
			managerCheckList = User.getUsersCheck(userList, currentUser.constructListWithManager());
		}
		else
		{
			managerCheckList = User.getUsersCheck(userList, report.getManagerList());
		}
		
    	memberAdapter = new MemberListViewAdapter(this, userList, managerCheckList);
    	View view = View.inflate(this, R.layout.me_member, null);
    	ListView userListView = (ListView) view.findViewById(R.id.userListView);
    	userListView.setAdapter(memberAdapter);
    	userListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				managerCheckList[position] = !managerCheckList[position];
				memberAdapter.setCheck(managerCheckList);
				memberAdapter.notifyDataSetChanged();
			}
		});

    	AlertDialog mDialog = new AlertDialog.Builder(this)
    							.setTitle("请选择汇报对象")
    							.setView(view)
    							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										List<User> managerList = new ArrayList<User>();
										for (int i = 0; i < managerCheckList.length; i++)
										{
											if (managerCheckList[i])
											{
												managerList.add(userList.get(i));
											}
										}

										report.setManagerList(managerList);
										managerTextView.setText(report.getManagersName());
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
    	mDialog.show();
    	
    	downloadAvatars();
    }
    
    private void showCCDialog()
    {
    	hideSoftKeyboard();
    	ccCheckList = User.getUsersCheck(userList, report.getCCList());
		
    	memberAdapter = new MemberListViewAdapter(this, userList, ccCheckList);
    	View view = View.inflate(this, R.layout.me_member, null);
    	ListView userListView = (ListView) view.findViewById(R.id.userListView);
    	userListView.setAdapter(memberAdapter);
    	userListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				ccCheckList[position] = !ccCheckList[position];
				memberAdapter.setCheck(ccCheckList);
				memberAdapter.notifyDataSetChanged();
			}
		});

    	AlertDialog mDialog = new AlertDialog.Builder(this)
    							.setTitle("请选择抄送对象")
    							.setView(view)
    							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										List<User> ccList = new ArrayList<User>();
										for (int i = 0; i < ccCheckList.length; i++)
										{
											if (ccCheckList[i])
											{
												ccList.add(userList.get(i));
											}
										}
										
										report.setCCList(ccList);
										ccTextView.setText(report.getCCsName());
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
    	mDialog.show();
    	
    	downloadAvatars();
    }

    private void saveReport(String prompt)
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
			Utils.showToast(EditReportActivity.this, "报告保存成功");
			finish();
		}
		else
		{
			Utils.showToast(EditReportActivity.this, "保存失败");
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
				AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
											.setTitle("无法提交报告")
											.setMessage("此报告为空报告")
											.setNegativeButton(R.string.confirm, null)
											.create();
				mDialog.show();
			}
			else if (appPreference.getCurrentGroupID() == -1)
			{
				report.setStatus(Report.STATUS_FINISHED);
				AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
											.setTitle("提示")
											.setMessage("报告提交成功")
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
			else
			{
				report.setStatus(Report.STATUS_SUBMITTED);		
				AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
										.setTitle("提示")
										.setMessage("报告提交成功")
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
			AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
												.setTitle("保存失败")
												.setNegativeButton(R.string.confirm, null)
												.create();
			mDialog.show();
		}
    }

    private void downloadAvatars()
    {
    	if (Utils.isNetworkConnected())
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
					String avatarPath = Utils.saveBitmapToFile(response.getBitmap(), HttpConstant.IMAGE_TYPE_AVATAR);
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
    	ReimApplication.showProgressDialog();
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
							ReimApplication.dismissProgressDialog();
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
							ReimApplication.dismissProgressDialog();
							Utils.showToast(EditReportActivity.this, "获取详细信息失败");
						}
					});
				}
			}
		});
    }
    
    private void sendCreateReportRequest(final String commentContent)
    {
    	ReimApplication.showProgressDialog();

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
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{							
							ReimApplication.dismissProgressDialog();
							Utils.showToast(EditReportActivity.this, "评论发表成功");
						}
					});
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Utils.showToast(EditReportActivity.this, "评论发表失败, " + response.getErrorMessage());
						}
					});					
				}
			}
		});
    }
    
    private void sendModifyReportRequest(final String commentContent)
    {
    	ReimApplication.showProgressDialog();
    	
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
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Utils.showToast(EditReportActivity.this, "评论发表成功");
						}
					});
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Utils.showToast(EditReportActivity.this, "评论发表失败, " + response.getErrorMessage());
						}
					});					
				}
			}
		});
    }
}
package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import netUtils.HttpConstant;
import netUtils.Request.BaseRequest.HttpConnectionCallback;
import netUtils.Request.Category.CreateCategoryRequest;
import netUtils.Request.Group.CreateGroupRequest;
import netUtils.Request.Group.DeleteGroupRequest;
import netUtils.Request.Item.CreateItemRequest;
import netUtils.Request.Item.DeleteItemRequest;
import netUtils.Request.Item.GetItemsRequest;
import netUtils.Request.Report.CreateReportRequest;
import netUtils.Request.Report.DeleteReportRequest;
import netUtils.Request.Report.ModifyReportRequest;
import netUtils.Request.Tag.CreateTagRequest;
import netUtils.Request.Tag.DeleteTagRequest;
import netUtils.Request.Tag.ModifyTagRequest;
import netUtils.Request.User.ChangePasswordRequest;
import netUtils.Request.User.ForgotPasswordRequest;
import netUtils.Request.User.InviteReplyRequest;
import netUtils.Request.User.InviteRequest;
import netUtils.Request.User.RegisterRequest;
import netUtils.Request.User.ResetPasswordRequest;
import netUtils.Request.User.SubordinatesInfoRequest;
import netUtils.Response.Category.CreateCategoryResponse;
import netUtils.Response.Group.CreateGroupResponse;
import netUtils.Response.Group.DeleteGroupResponse;
import netUtils.Response.Item.CreateItemResponse;
import netUtils.Response.Item.DeleteItemResponse;
import netUtils.Response.Item.GetItemsResponse;
import netUtils.Response.Report.CreateReportResponse;
import netUtils.Response.Report.DeleteReportResponse;
import netUtils.Response.Report.ModifyReportResponse;
import netUtils.Response.Tag.CreateTagResponse;
import netUtils.Response.Tag.DeleteTagResponse;
import netUtils.Response.Tag.ModifyTagResponse;
import netUtils.Response.User.ChangePasswordResponse;
import netUtils.Response.User.ForgotPasswordResponse;
import netUtils.Response.User.InviteReplyResponse;
import netUtils.Response.User.InviteResponse;
import netUtils.Response.User.RegisterResponse;
import netUtils.Response.User.ResetPasswordResponse;
import netUtils.Response.User.SubordinatesInfoResponse;
import classes.Category;
import classes.Item;
import classes.Report;
import classes.Tag;
import classes.User;
import classes.Adapter.ItemListViewAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.Fragment;

public class ReimFragment extends Fragment {

	private View view;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_reimbursement, container, false);
	    return view;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState);
        
		Button button=(Button) getActivity().findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					User user = new User();
					user.setEmail("y@rushucloud.com");
					user.setPassword("meiyoumima");
//					InviteRequest request = new InviteRequest(HttpConstant.REQUEST_TYPE_EMAIL, "y@rushucloud.com");
//					request.sendRequest(new HttpConnectionCallback()
//					{
//						public void execute(Object object)
//						{
//							InviteResponse response2 = new InviteResponse(object);
//						}
//					});
					
//					InviteReplyRequest request = new InviteReplyRequest(HttpConstant.INVITE_REPLY_AGREE, 1);
//					request.sendRequest(new HttpConnectionCallback()
//					{
//						public void execute(Object object)
//						{
//							InviteReplyResponse response2 = new InviteReplyResponse(object);
//						}
//					});
					
//					ForgotPasswordRequest request = new ForgotPasswordRequest(0, "y@rushucloud.com");
//					request.sendRequest(new HttpConnectionCallback()
//					{
//						
//						@Override
//						public void execute(Object httpResponse)
//						{
//							ForgotPasswordResponse response = new ForgotPasswordResponse(httpResponse);
//						}
//					});
					
//					ResetPasswordRequest request = new ResetPasswordRequest(user, 1, "dd1ccb");
//					request.sendRequest(new HttpConnectionCallback()
//					{
//						
//						@Override
//						public void execute(Object httpResponse)
//						{
//							ResetPasswordResponse response = new ResetPasswordResponse(httpResponse);
//						}
//					});
					
//			        UserInfo userInfo = UserInfo.getUserInfo();
//			        userInfo.("debug@rushucloud.com");
//			        ReimApplication reimApp = (ReimApplication)getActivity().getApplication();
//			        reimApp.saveUserInfo();       

//					DBManager dbManager = DBManager.getDataBaseManager(getActivity().getApplicationContext());
//					dbManager.openDatabase();
//					dbManager.modifyItem(null, null);
//					dbManager.findMyItems();
					
//					Category category = new Category();
//					category.setName("bbb");
//					category.setParentID(0);
//					category.setLimit(500);
//					category.setGroupID(2);
//					category.setPreBillable(true);
//					category.setId(1);
					
//					CreateCategoryRequest request = new CreateCategoryRequest(category);
//					request.sendRequest(new HttpConnectionCallback()
//					{
//						public void execute(Object object)
//						{
//							CreateCategoryResponse response2 = new CreateCategoryResponse(object);
//						}
//					});
					
//					CreateGroupRequest request = new CreateGroupRequest("rushu");
//					request.sendRequest(new HttpConnectionCallback()
//					{
//						
//						@Override
//						public void execute(Object httpResponse)
//						{
//							CreateGroupResponse response = new CreateGroupResponse(httpResponse);
//						}
//					});

//					Item item=new Item();
//					Category category = new Category();
//					category.setId(1);
//					List<User> userList = new ArrayList<User>();
//					for (int i = 0; i < 4; i++)
//					{
//						User user = new User();
//						user.setId(i+2);
//						userList.add(user);						
//					}
//					item.setId(1);
//					item.setRelevantUsers(userList);
//					item.setAmount(50);
//					item.setCategory(category);
//					item.setMerchant("McDonalds");
//					item.setBillable(true);
//					item.setImageID(0);
//					item.setConsumedDate(new Date());
//					
//					CreateItemRequest request = new CreateItemRequest(item);
//					request.sendRequest(new HttpConnectionCallback()
//					{
//						public void execute(Object httpResponse)
//						{
//							CreateItemResponse response = new CreateItemResponse(httpResponse);
//						}
//					});

//					SyncDataRequest request = new SyncDataRequest(0,0);
//					request.sendRequest(new HttpConnectionCallback()
//					{
//						public void execute(String response)
//						{
//							SyncDataResponse response2 = new SyncDataResponse(response);
//							System.out.println(response2.getCode());
//						}
//					});

//					List<Item> itemList = new ArrayList<Item>();
//					for (int i = 0; i < 4; i++)
//					{
//						Item item = new Item();
//						item.setId(i+3);
//						itemList.add(item);
//					}
					
//					Report report = new Report();
//					report.setItemIDs(itemList);
//					report.setTitle("Temp");
//					report.setId(2);
//					report.setStatus(2);
//					
//					DeleteReportRequest request = new DeleteReportRequest(report);
//					request.sendRequest(new HttpConnectionCallback()
//					{
//						
//						@Override
//						public void execute(Object httpResponse)
//						{
//							DeleteReportResponse response = new DeleteReportResponse(httpResponse);
//						}
//					});
					
//					Tag tag = new Tag();
//					tag.setName("Tea");
//					tag.setId(1);
//					
//					DeleteTagRequest request = new DeleteTagRequest(tag);
//					request.sendRequest(new HttpConnectionCallback()
//					{
//						public void execute(Object httpResponse)
//						{
//							DeleteTagResponse response2 = new DeleteTagResponse(httpResponse);
//						}
//					});
					
					SubordinatesInfoRequest request = new SubordinatesInfoRequest(0,20);
					request.sendRequest(new HttpConnectionCallback()
					{
						public void execute(Object httpResponse)
						{
							SubordinatesInfoResponse response2 = new SubordinatesInfoResponse(httpResponse);
						}
					});
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			};
		});
 
		Button addButton = (Button)getActivity().findViewById(R.id.addButton);
		addButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Toast.makeText(getActivity(), "test", Toast.LENGTH_SHORT).show();
			}
		});
		
		List<Item> list=new ArrayList<Item>();
		for (int i = 0; i < 20; i++)
		{
			Category category = new Category();
			category.setName(Integer.toString(i));
			Report report = new Report();
			report.setTitle("my report "+i);
			Bitmap photo = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_launcher);
			Item item=new Item();
			item.setAmount(i*20);
			item.setCategory(category);
			item.setNote("object "+i);
			item.setBelongReport(report);
			item.setImage(photo);
			list.add(item);
		}
		ItemListViewAdapter adapter = new ItemListViewAdapter(getActivity(), list);
		ListView listView=(ListView)getActivity().findViewById(R.id.itemListView);
		listView.setAdapter(adapter);
	}  
}

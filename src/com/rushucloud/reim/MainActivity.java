package com.rushucloud.reim;

import database.DBManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBarActivity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;
	
public class MainActivity extends ActionBarActivity {

	private long exitTime;
	
	private FragmentTabHost tabHost;
	private int tabIndex = 0;
	
	private Class<?> fragmentList[] = {ReimFragment.class, ReportFragment.class, StatisticsFragment.class, MeFragment.class};
	private int imageViewList[] = {R.drawable.tab_item_reim,R.drawable.tab_item_report,R.drawable.tab_item_statistics, R.drawable.tab_item_me};
	private int textviewList[] = {R.string.reimbursement, R.string.report, R.string.statistics, R.string.me};
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dataInitialise();
		tabHostInitialse();
	}

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {
        	if(System.currentTimeMillis()-exitTime>2000)
        	{
        		Toast.makeText(MainActivity.this, "再按一次返回键退出程序", Toast.LENGTH_LONG).show();
        		exitTime=System.currentTimeMillis();
        	}
        	else 
        	{
				finish();
				DBManager dbManager = DBManager.getDBManager();
				dbManager.close();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
            return true;
        } 
        else 
        {
            return super.onKeyDown(keyCode, event);
        }
    }
    
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void dataInitialise()
	{
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null)
		{
			tabIndex = bundle.getInt("tabIndex");			
		}
	}

	private void tabHostInitialse()
    {		
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		for(int i = 0; i < 4; i++)
		{
			View view = layoutInflater.inflate(R.layout.tab_item, (ViewGroup)null, false);

			Drawable drawableTop = getResources().getDrawable(imageViewList[i]);
			drawableTop.setBounds(0, 5, drawableTop.getMinimumWidth(), drawableTop.getMinimumHeight()+5);
			
			TextView textView = (TextView) view.findViewById(R.id.textView);		
			textView.setText(getText(textviewList[i]));			
			textView.setCompoundDrawablePadding(5);
			textView.setCompoundDrawables(null, drawableTop, null, null);			
			
			TabSpec tabSpec = tabHost.newTabSpec(getText(textviewList[i]).toString()).setIndicator(view);
			tabHost.addTab(tabSpec, fragmentList[i], null);
			tabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
		}

	    //tabHost.setCurrentTab(tabIndex);
		tabHost.setCurrentTab(2);		
    }
}

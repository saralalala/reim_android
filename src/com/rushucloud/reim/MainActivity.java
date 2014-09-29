package com.rushucloud.reim;

import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBarActivity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;
	
public class MainActivity extends ActionBarActivity {

	private FragmentTabHost tabHost;
	private int tabIndex;
	
	private Class<?> fragmentList[] = {ReimFragment.class, ReportFragment.class, StatisticsFragment.class, MeFragment.class};
	private int imageViewList[] = {R.drawable.tab_item_reim,R.drawable.tab_item_report,R.drawable.tab_item_statistics, R.drawable.tab_item_me};
	private int textviewList[] = {R.string.reimbursement, R.string.report, R.string.statistics, R.string.me};
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tabHostInitialse();
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
			
			TextView textView = (TextView) view.findViewById(R.id.textview);		
			textView.setText(getText(textviewList[i]));			
			textView.setCompoundDrawablePadding(5);
			textView.setCompoundDrawables(null, drawableTop, null, null);			
			
			TabSpec tabSpec = tabHost.newTabSpec(getText(textviewList[i]).toString()).setIndicator(view);
			tabHost.addTab(tabSpec, fragmentList[i], null);
			tabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
		}

		tabIndex=0;
	    tabHost.setCurrentTab(tabIndex);		
    }
}

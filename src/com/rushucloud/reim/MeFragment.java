package com.rushucloud.reim;

import classes.Adapter.MeListViewAdapater;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.support.v4.app.Fragment;

public class MeFragment extends Fragment
{
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
	    return inflater.inflate(R.layout.fragment_me, container, false);  
	}
	
	public void onActivityCreated(Bundle savedInstanceState)
	{  
        super.onActivityCreated(savedInstanceState);
        viewInitialise();
    }
	
	private void viewInitialise()
	{
        MeListViewAdapater adapter = new MeListViewAdapater(getActivity()); 
        ListView meListView = (ListView)getActivity().findViewById(R.id.meListView);
        meListView.setAdapter(adapter);
        meListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if (position == 0)
				{
					startActivity(new Intent(getActivity(), ProfileActivity.class));
				}
			}
		});
	}
}

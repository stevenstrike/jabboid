/**
 * Main activity launched by starting the app, 
 * offer 3 GOTO Activity options : 
 * chat, contacts list and QR-Code Scan
 * 
 * @author 		Steven Jalabert <steven.jalabert@gmail.com>
 * @version 	rev4
 * @since		2014-06-10
 */
package fr.jbteam.jabboid.core;

import fr.jbteam.jabboid.R;
import fr.jbteam.jabboid.ui.*;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends ActionBarActivity 
{
	private static MainActivity instance;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		instance = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) 
		{
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.quit) 
		{
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment 
	{

		public PlaceholderFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	public void callScanner(View view)
	{
		try
		{
		     Intent k = new Intent(this, CameraActivity.class);
		     startActivity(k);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void callChatClient(View view)
	{
		try
		{
		     Intent k = new Intent(this, ChatClient.class);
		     startActivity(k);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void callContactsList(View view)
	{
		try
		{
		     Intent k = new Intent(this, ContactsListActivity.class);
		     startActivity(k);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static MainActivity getContext()
	{
    	return instance;
    }
}

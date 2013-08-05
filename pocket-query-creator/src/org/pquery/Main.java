/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pquery;

import java.util.Date;

import junit.framework.Assert;

import net.htmlparser.jericho.Config;

import org.pquery.dao.PQ;
import org.pquery.fragments.MyDialogFragment;
import org.pquery.fragments.PQListFragment;
import org.pquery.fragments.PQListFragment.PQClickedListener;
import org.pquery.fragments.ProgressBoxFragment;
import org.pquery.fragments.ProgressBoxFragment.ProgressBoxFragmentListener;
import org.pquery.service.CreatePQResult;
import org.pquery.service.DownloadPQResult;
import org.pquery.service.PQService;
import org.pquery.service.PQServiceListener;
import org.pquery.service.RetrievePQListResult;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.util.Util;
import org.pquery.webdriver.ProgressInfo;

import android.R.color;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class Main extends SherlockFragmentActivity implements PQClickedListener, PQServiceListener, ProgressBoxFragmentListener {

	private boolean doDialog;
	private boolean onSaveInstanceStateCalled;
	
	private PQService service;

	private enum ServiceStatus { NotConnected, Connected, ServiceBusy };

	private ServiceStatus serviceStatus;
	//private long pqListTimestamp;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Logger.d("enter");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We only want single instance of this activity at top
		// If launched via a notification and by normal icon android can try
		//to create another copy of activity down stack
		// Don't want that so pop superfluus activity
		//
		// http://stackoverflow.com/questions/4341600/how-to-prevent-multiple-instances-of-an-activity-when-it-is-launched-with-differ
		if (!isTaskRoot()) {
			    final Intent intent = getIntent();
			    final String intentAction = intent.getAction(); 
			    if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
			        Logger.w("Not root. Finishing Main Activity instead of launching");
			        finish();
			        return;       
			    }
		}
		
		// Make Jericho form parsing case sensitive
		Config.CurrentCompatibilityMode = Config.CompatibilityMode.MOZILLA;

		// Enable logging (if pref set)
		// If first time logging has been initialised a new log file will be created
		Logger.setEnable(Prefs.getDebug(this));
		Logger.d("enter");
		
		// Enable progress bar at top of window
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.main2);

		if (savedInstanceState!=null) {
			//this.pqListTimestamp = savedInstanceState.getLong("pqListTimestamp");
		}

		String title = getIntent().getStringExtra("title");
		if (title!=null)
			doDialog = true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		setIntent(intent);
		String title = intent.getStringExtra("title");
		if (title!=null)
			doDialog = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Logger.d("enter");
		onSaveInstanceStateCalled = false;
		
		long time = Prefs.getPQListStateTimestamp(this);
		PQListFragment pqList = (PQListFragment) getSupportFragmentManager().findFragmentById(R.id.pq_list_fragment);
		
		// First check if we have a PQ list stored
		if (time!=0)
		{	
			// OK we know we have a PQ list

			// First check if it is too old
			// If so erase it and set list to be empty
			
			if (new Date().getTime() - time > 1000 * 60 * 15) {
				Prefs.erasePQListState(this);
				pqList.updateList(null);
			}
			else {
				pqList.updateList(Prefs.getPQListState(this));
			
			}
			//this.pqListTimestamp = time;
		//}
		}
		else {
			pqList.updateList(null);
		}
		
		if (doDialog) {
			String title = getIntent().getStringExtra("title");
			String message = getIntent().getStringExtra("message");
			int notificationId = getIntent().getIntExtra("notificationId",0);
			
			if (title!=null && message!=null) {
				onServiceOperationResult(title, message, notificationId);
			}

			getIntent().removeExtra("title");
			getIntent().removeExtra("message");
			getIntent().removeExtra("notificationId");

		}
		doDialog = false;

		doBindService();


	}

	@Override
	protected void onPause() {
		super.onPause();
		Logger.d("enter");
		doUnbindService();
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Logger.d("enter");
		//outState.putLong("pqListTimestamp", pqListTimestamp);
		onSaveInstanceStateCalled = true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {


		if (serviceStatus == ServiceStatus.ServiceBusy) {

			setSupportProgressBarVisibility(true);
			setSupportProgressBarIndeterminateVisibility(true);
		} else {
			setSupportProgressBarVisibility(false);
			setSupportProgressBarIndeterminateVisibility(false);
		}


		if (serviceStatus == ServiceStatus.Connected) {
			menu.add(0, R.string.create, 0, R.string.create)
			.setIcon(R.drawable.content_new)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

			menu.add(0, R.string.get_pq_list, 0, R.string.get_pq_list)
			.setIcon(R.drawable.navigation_refresh)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		} else {

			menu.add(0, R.string.create, 0, R.string.create)
			.setIcon(Util.toGrey(getResources(), R.drawable.content_new))
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

			menu.add(0, R.string.get_pq_list, 0, R.string.get_pq_list)
			.setIcon(Util.toGrey(getResources(), R.drawable.navigation_refresh))
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		}

		menu.add(0, R.string.help, 0, R.string.help)
		.setIcon(R.drawable.action_help)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(0, R.string.settings, 0, R.string.settings)
		.setIcon(R.drawable.action_settings)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		menu.add(0, R.string.about, 0, R.string.about)
		.setIcon(R.drawable.action_about)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		/*
		Dialog myDialog = new Dialog(this, R.style.CustomDialogTheme);
		 myDialog.setContentView(R.layout.rob);


	        final String mimeType = "text/html";
	        final String encoding = "utf-8";

		 WebView wv;

	        String html = "<font color='#ffffff'>" + 

			"<style type='text/css'>" +
			"a:link {color: #0066FF; text-decoration: underline; }" +
			"a:active {color: #0066FF; text-decoration: underline; }" +
			"a:visited {color: #0066FF; text-decoration: underline; }" +
			"a:hover {color: #0066FF; text-decoration: underline; }" +
			"li {margin: 5px}" +
			"</style> " +
			
			"Welcome to Pocket Query Creator<p>" +
			"Enter your Geocaching.com in Settings" +
			"</font>";

	        wv = (WebView) myDialog.findViewById(R.id.webview);
	        wv.setBackgroundColor(getResources().getColor(color.black));
	        wv.loadData(html, mimeType, encoding);

	        
	        
		myDialog.show();
		*/
		
		
		switch (item.getItemId()) {
		case R.string.create:
			if (Prefs.getUsername(this).length() == 0 || Prefs.getPassword(this).length() ==0) {
				Toast.makeText(this, "First enter your premium geocaching.com account credentials on the settings page", Toast.LENGTH_LONG).show();
				return true;
			}
			if (serviceStatus != ServiceStatus.Connected)
				return true;

			startActivity(new Intent(this, CreateActivity.class));
			break;
		case R.string.get_pq_list:
			if (Prefs.getUsername(this).length() == 0 || Prefs.getPassword(this).length() ==0) {
				Toast.makeText(this, "First enter your premium geocaching.com account credentials on the settings page", Toast.LENGTH_LONG).show();
				return true;
			}
			if (serviceStatus != ServiceStatus.Connected)
				return true;

			Intent intent = new Intent(this, PQService.class);
			intent.putExtra("operation", PQService.OPERATION_REFRESH);
			startService(intent);
			break;
		case R.string.settings:
			startActivity(new Intent(this, PreferencesFromXml.class));
			break;
		case R.string.about:
			startActivity(new Intent(this, About.class));
			break;
		case R.string.help:
			startActivity(new Intent(this, Help.class));
			break;
		}
		

		return true;
	}

	private boolean isServiceBound;

	private ServiceConnection connection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
			if (onSaveInstanceStateCalled) {
				Logger.w("Skipping service connected as onSaveInstanceState already called");
			} else {
			Logger.d("connect");
				service = ((PQService.LocalBinder)serviceBinder).getService();
				service.registerClient(Main.this);

				checkActionBar();
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			Logger.d("disconnect");
			service = null;

			checkActionBar();
		}
	};

	private void doBindService() {
		isServiceBound = bindService(new Intent(getApplicationContext(), PQService.class), connection, BIND_AUTO_CREATE);
		Logger.d("[isServiceBound=" + isServiceBound + "]");
	}

	/**
	 * Stop being interested in service If still connected to service, sent
	 * unregister message to it
	 */
	private void doUnbindService() {
		Logger.d("[isServiceBound=" + isServiceBound + "]");

		if (isServiceBound) {

			if (service!=null)
				service.unRegisterClient(this);

			unbindService(connection);
			isServiceBound = false;
		}

		service = null;
		checkActionBar();
	}

	private class PopupBar implements ActionMode.Callback {
		private PQ pq;
		public PopupBar(PQ pq) {
			this.pq = pq;
		}
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			menu.add("Download")
			.setIcon(R.drawable.av_download)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			return true;
		}
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			Intent intent = new Intent(getApplicationContext(), PQService.class);
			//            Messenger messenger = new Messenger(handler);
			//            intent.putExtra("messenger", messenger);
			intent.putExtra("operation", PQService.OPERATION_DOWNLOAD);
			intent.putExtra("pq", (Parcelable) pq);
			startService(intent);

			mode.finish();
			return true;
		}
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			actionMode = null;
		}
	}

	ActionMode actionMode;

	/**
	 * The fragment listing the PQ has been clicked on
	 * We open popup bar at top to allow PQ to be downloaded
	 */
	@Override
	public void onPQClicked(PQ pq) {

		if (actionMode!=null) {
			// PQ was clicked whilst bar was open for a previous PQ click
			// Must manually close previous one
			// (Attempting to immediately open another bar on the new pq doesn't seem to work ?)
			actionMode.finish();
		}
		else {
			// Open top bar to allow selection for PQ download
			if (serviceStatus == ServiceStatus.Connected)
				actionMode = startActionMode(new PopupBar(pq));
		}
	}

	@Override
	public void onServiceRetrievePQList(RetrievePQListResult pqListResult) {
		PQListFragment pqList = (PQListFragment) getSupportFragmentManager().findFragmentById(R.id.pq_list_fragment);

		//pqListTimestamp = Prefs.getPQListStateTimestamp(this);
		pqList.updateList(pqListResult.pqs);

		if (pqListResult.failure != null) {
			MyDialogFragment dialog = MyDialogFragment.newInstance("Failed", pqListResult.failure.toString());
			dialog.show(getSupportFragmentManager(), "dialog");
		}

		checkActionBar();
	}

	@Override
	public void onServiceProgressInfo(ProgressInfo progressInfo) {
		Assert.assertNotNull(progressInfo);

		ProgressBoxFragment box = getProgressBoxFragment();

		setSupportProgressBarVisibility(true);
		int a = (Window.PROGRESS_END - Window.PROGRESS_START) / 100 * progressInfo.percent;
		setSupportProgress(a);
		checkActionBar();

		box.setText(progressInfo.htmlMessage);
	}


	private void checkActionBar() {
		if (serviceStatus != calculateServiceStatus()) {
			serviceStatus = calculateServiceStatus();

			supportInvalidateOptionsMenu();

			if (serviceStatus != ServiceStatus.ServiceBusy)
				hideProgressBoxFragment();
		}
	}

	private ServiceStatus calculateServiceStatus() {
		if (service == null)
			return ServiceStatus.NotConnected;
		if (service.isOperationInProgress())
			return ServiceStatus.ServiceBusy;
		return ServiceStatus.Connected;
	}


	private ProgressBoxFragment getProgressBoxFragment() {
		ProgressBoxFragment box = (ProgressBoxFragment) getSupportFragmentManager().findFragmentByTag("robtag");

		if (box!=null)
			return box;

		// Fragment is not currently up. Add it
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		box = new ProgressBoxFragment();
		fragmentTransaction.add(R.id.fragment_content, box, "robtag");
		fragmentTransaction.commit();

		return box;  
	}

	private void hideProgressBoxFragment() {
		ProgressBoxFragment box = (ProgressBoxFragment) getSupportFragmentManager().findFragmentByTag("robtag");

		if (box==null)
			return;

		// Fragment is up, need to remove it
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.remove(box);
		fragmentTransaction.commit();
	}

	/**
	 * Fragment containing service progress text has been clicked. Means
	 * user wants to cancel current service and abort;
	 */
	@Override
	public void onProgressBoxFragmentClicked() {
		if (serviceStatus == ServiceStatus.ServiceBusy)
			service.cancelInProgress();
	}

	@Override
	public void onServiceStartingTask() {
		checkActionBar();
	}

	@Override
	public void onServiceStoppedTask() {
		checkActionBar();
	}

	@Override
	public void onServiceOperationResult(String title, String message,
			int notificationId) {

		MyDialogFragment dialog = MyDialogFragment.newInstance(title, message);
		dialog.show(getSupportFragmentManager(), "dialog");

		checkActionBar();

		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(notificationId);
	}


}

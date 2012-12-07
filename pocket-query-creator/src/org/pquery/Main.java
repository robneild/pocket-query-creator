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

import org.pquery.dao.PQ;
import org.pquery.fragments.MyDialogFragment;
import org.pquery.fragments.PQListFragment;
import org.pquery.fragments.PQListFragment.PQClickedListener;
import org.pquery.fragments.ProgressBoxFragment;
import org.pquery.fragments.ProgressBoxFragment.ProgressBoxFragmentListener;
import org.pquery.service.CreatePQResult;
import org.pquery.service.DownloadPQResult;
import org.pquery.service.NotificationUtil;
import org.pquery.service.PQService;
import org.pquery.service.PQServiceListener;
import org.pquery.service.RetrievePQListResult;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.util.Util;
import org.pquery.webdriver.ProgressInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class Main extends SherlockFragmentActivity implements PQClickedListener, PQServiceListener, ProgressBoxFragmentListener {

    private boolean doDialog;

    private PQService service;

    private enum ServiceStatus { NotConnected, Connected, ServiceBusy };

    private ServiceStatus serviceStatus;
    private long pqListTimestamp;

    /** Messenger for communicating with service. */
    //    private Messenger messengerOut;

    //    final Messenger messengerIn = new Messenger(new IncomingHandler());

    //    private Handler handler = new Handler() {
    //        public void handleMessage(Message message) {
    //            if (message.obj instanceof RetrievePQListResult) {
    //                
    //                // Got the results from getting list of pocket query
    //                // Could be a list of PQ or an error
    //                RetrievePQListResult result = (RetrievePQListResult) message.obj;
    //                PQListFragment pqList = (PQListFragment) getSupportFragmentManager().findFragmentById(R.id.pq_list_fragment);
    //
    //                if (result.failure != null) {
    //                    ErrorDialogFragment dialog = ErrorDialogFragment.newInstance(result.failure.toString());
    //                    dialog.show(getSupportFragmentManager(), "dialog");
    //                } else {
    //                    pqList.updateList(result.pqs);
    //                }
    //            } else if (message.obj instanceof DownloadPQResult) {
    //                
    //                // Got results from downloading pocket query
    //                
    //                DownloadPQResult result = (DownloadPQResult) message.obj;
    //                
    //                if (result.failure != null) {
    //                    ErrorDialogFragment dialog = ErrorDialogFragment.newInstance(result.failure.toString());
    //                    dialog.show(getSupportFragmentManager(), "dialog");
    //                } else {
    //                    ErrorDialogFragment dialog = ErrorDialogFragment.newInstance("Pocket Query downloaded into "+result.fileNameDownloaded.getAbsolutePath());
    //                    dialog.show(getSupportFragmentManager(), "dialog");
    //                }
    //            }
    //        };
    //    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.setEnable(Prefs.getDebug(this));

        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.main2);

        if (savedInstanceState!=null) {
            this.pqListTimestamp = savedInstanceState.getLong("pqListTimestamp");
        }

        String title = getIntent().getStringExtra("title");
        if (title!=null)
            doDialog = true;

        //PQListFragment pqList = (PQListFragment) getSupportFragmentManager().findFragmentById(R.id.pq_list_fragment);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        String title = intent.getStringExtra("title");
        if (title!=null)
            doDialog = true;

        //        if (intent!=null) {
        //            String title = intent.getStringExtra("title");
        //            String message = intent.getStringExtra("message");
        //
        //            if (title!=null && message!=null) {
        //                MyDialogFragment dialog = MyDialogFragment.newInstance(title, message);
        //                dialog.show(getSupportFragmentManager(), "dialog");
        //            }
        //        }
    }

    @Override
    protected void onResume() {
        super.onStart();

        long time = Prefs.getPQListStateTimestamp(this);

        if (time> pqListTimestamp) {
            if (new Date().getTime() - time > 1000 * 60 * 60) {
                Prefs.erasePQListState(this);
                time = 0;
            }
            PQListFragment pqList = (PQListFragment) getSupportFragmentManager().findFragmentById(R.id.pq_list_fragment);
            pqList.updateList(Prefs.getPQListState(this));
            this.pqListTimestamp = time;
        }

        if (doDialog) {
            String title = getIntent().getStringExtra("title");
            String message = getIntent().getStringExtra("message");

            if (title!=null && message!=null) {
                MyDialogFragment dialog = MyDialogFragment.newInstance(title, message);
                dialog.show(getSupportFragmentManager(), "dialog");
                new NotificationUtil(this).dismissNotification(getIntent().getIntExtra("notificationId",0));
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
        doUnbindService();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("pqListTimestamp", pqListTimestamp);
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

            menu.add(0, R.string.refresh, 0, R.string.refresh)
            .setIcon(R.drawable.navigation_refresh)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else {
            
            menu.add(0, R.string.create, 0, R.string.create)
            .setIcon(Util.toGrey(getResources(), R.drawable.content_new))
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

            menu.add(0, R.string.refresh, 0, R.string.refresh)
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

        switch (item.getItemId()) {
        case R.string.create:
            if (Prefs.getUsername(this).length() == 0 || Prefs.getPassword(this).length() ==0) {
                Toast.makeText(this, "Enter your credentials on the settings page", Toast.LENGTH_LONG).show();
                return true;
            }
            startActivity(new Intent(this, CreateActivity.class));
            break;
        case R.string.refresh:
            if (Prefs.getUsername(this).length() == 0 || Prefs.getPassword(this).length() ==0) {
                Toast.makeText(this, "Enter your credentials on the settings page", Toast.LENGTH_LONG).show();
                return true;
            }

            Intent intent = new Intent(this, PQService.class);
            //            Messenger messenger = new Messenger(handler);
            //            intent.putExtra("messenger", messenger);
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
            service = ((PQService.LocalBinder)serviceBinder).getService();
            service.registerClient(Main.this);

            checkActionBar();
        }

        public void onServiceDisconnected(ComponentName className) {
            Logger.d("enter");
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
        if (isServiceBound) {

            if (service!=null)
                service.unRegisterClient(this);

            unbindService(connection);
            isServiceBound = false;
        }

        service = null;
        checkActionBar();
    }

    private final class PopupBar implements ActionMode.Callback {
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
        }
    }


    @Override
    public void onPQClicked(PQ pq) {
        startActionMode(new PopupBar(pq));
    }

    @Override
    public void onServicePQDownloaded(DownloadPQResult result) {

        MyDialogFragment dialog = MyDialogFragment.newInstance(result.getTitle(), result.getMessage());
        dialog.show(getSupportFragmentManager(), "dialog");

        checkActionBar();
    }

    @Override
    public void onServiceRetrievePQList(RetrievePQListResult pqListResult) {
        PQListFragment pqList = (PQListFragment) getSupportFragmentManager().findFragmentById(R.id.pq_list_fragment);

        pqListTimestamp = Prefs.getPQListStateTimestamp(this);
        pqList.updateList(pqListResult.pqs);

        if (pqListResult.failure != null) {
            MyDialogFragment dialog = MyDialogFragment.newInstance("Failed", pqListResult.failure.toString());
            dialog.show(getSupportFragmentManager(), "dialog");
        }

        checkActionBar();
    }

    @Override
    public void onServiceProgressInfo(ProgressInfo progressInfo) {
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

    @Override
    public void onProgressBoxFragmentClicked() {
        if (service!=null)
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
    public void onServicePQCreated(CreatePQResult createPQResult) {
        checkActionBar();
    }

}

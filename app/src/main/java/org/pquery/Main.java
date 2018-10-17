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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.SupportActionModeWrapper;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import net.htmlparser.jericho.Config;

import org.pquery.dao.DownloadablePQ;
import org.pquery.fragments.MyDialogFragment;
import org.pquery.fragments.PQListFragment;
import org.pquery.fragments.PQListFragment.PQClickedListener;
import org.pquery.fragments.ProgressBoxFragment;
import org.pquery.fragments.ProgressBoxFragment.ProgressBoxFragmentListener;
import org.pquery.service.PQService;
import org.pquery.service.PQServiceListener;
import org.pquery.service.RetrievePQListResult;
import org.pquery.util.Assert;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.util.Util;
import org.pquery.webdriver.ProgressInfo;

import java.io.File;
import java.util.Date;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class Main extends AppCompatActivity implements PQClickedListener, PQServiceListener, ProgressBoxFragmentListener {

    private static final int CREATE_REQUEST_CODE = 1;
    private static final int DOWNLOAD_REQUEST_CODE = 2;

    private boolean doDialog;
    private boolean onSaveInstanceStateCalled;

    private PQService service;

    private enum ServiceStatus {NotConnected, Connected, ServiceBusy}

    ;

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

        String title = getIntent().getStringExtra("title");
        if (title != null)
            doDialog = true;


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        String title = intent.getStringExtra("title");
        if (title != null)
            doDialog = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d("enter");
        onSaveInstanceStateCalled = false;

        long time = Prefs.getPQListStateTimestamp(this);
        PQListFragment pqList = (PQListFragment) getFragmentManager().findFragmentById(R.id.pq_list_fragment);

        // First check if we have a DownloadablePQ list stored
        if (time != 0) {
            // OK we know we have a DownloadablePQ list

            // First check if it is too old
            // If so erase it and set list to be empty

            if (new Date().getTime() - time > 1000 * 60 * 15) {
                Prefs.erasePQListState(this);
                pqList.updateList(null, null);
            } else {
                pqList.updateList(Prefs.getDPQListState(this), Prefs.getRPQListState(this));

            }
            //this.pqListTimestamp = time;
            //}
        } else {
            pqList.updateList(null, null);
        }

        if (doDialog) {
            String title = getIntent().getStringExtra("title");
            String message = getIntent().getStringExtra("message");
            int notificationId = getIntent().getIntExtra("notificationId", 0);
            File fileNameDownloaded = (File) (getIntent().getExtras().get("fileNameDownloaded"));
            if (title != null && message != null) {
                onServiceOperationResult(title, message, notificationId,fileNameDownloaded);
            }

            getIntent().removeExtra("title");
            getIntent().removeExtra("message");
            getIntent().removeExtra("notificationId");
            getIntent().removeExtra("fileNameDownloaded");
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

            setProgressBarVisibility(true);
            setProgressBarIndeterminateVisibility(true);
        } else {
            setProgressBarVisibility(false);
            setProgressBarIndeterminateVisibility(false);
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

        //ProgressBoxFragment box = getProgressBoxFragment();
        //box.setText("dddddd");

       // PQListFragment pqList = (PQListFragment) getFragmentManager().findFragmentById(R.id.pq_list_fragment);
        //pqList.updateList(null, null);



       // if (0==0) return true;

        switch (item.getItemId()) {
            case R.string.create:
                if (Prefs.getUsername(this).length() == 0 || Prefs.getPassword(this).length() == 0) {
                    Toast.makeText(this, R.string.enter_gc_credentials, Toast.LENGTH_LONG).show();
                    return true;
                }
                if (serviceStatus != ServiceStatus.Connected)
                    return true;

                // Check if can create files in configured location, before going to next activity
                if (canCreateFile()) {
                    startActivity(new Intent(this, CreateFiltersActivity.class));
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, CREATE_REQUEST_CODE);

                            // Waiting for permission response
                            // Silently return false for now
                            return true;
                        }
                    }

                    // Unable to output to file. Don't move to next activity
                    Toast toast = Toast.makeText(this, R.string.unable_to_create_file, Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
            case R.string.get_pq_list:
                if (Prefs.getUsername(this).length() == 0 || Prefs.getPassword(this).length() == 0) {
                    Toast.makeText(this, R.string.enter_gc_credentials, Toast.LENGTH_LONG).show();
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
                service = ((PQService.LocalBinder) serviceBinder).getService();
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
        isServiceBound = bindService(new Intent(this, PQService.class), connection, BIND_AUTO_CREATE);
        Logger.d("[isServiceBound=" + isServiceBound + "]");
    }

    /**
     * Stop being interested in service If still connected to service, sent
     * unregister message to it
     */
    private void doUnbindService() {
        Logger.d("[isServiceBound=" + isServiceBound + "]");

        if (isServiceBound) {

            if (service != null)
                service.unRegisterClient(this);

            unbindService(connection);
            isServiceBound = false;
        }

        service = null;
        checkActionBar();
    }















    // Constructed when a pocket query to download is clicked upon

    private class PopupBar implements android.support.v7.view.ActionMode.Callback {

        private DownloadablePQ pq;

        /**
         * Called when a downloadable pocket query (in the list) is clicked upon
         * @param pq details about item selected
         */
        public PopupBar(DownloadablePQ pq) {
            this.pq = pq;
        }

        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
            MenuInflater dfds = actionMode.getMenuInflater();

            menu.add("Download")
                    .setIcon(R.drawable.av_download)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
            return false;
        }

        /** A pocket query was selected and "download" on the menu clicked */
        @Override
        public boolean onActionItemClicked(android.support.v7.view.ActionMode actionMode, MenuItem item) {
            if (canCreateFile()) {
                Intent intent = new Intent(Main.this, PQService.class);
                intent.putExtra("operation", PQService.OPERATION_DOWNLOAD);
                intent.putExtra("pq", (Parcelable) pq);
                startService(intent);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, DOWNLOAD_REQUEST_CODE);
                        // Waiting for permission response
                        // Silently return false for now
                        actionMode.finish();
                        return true;
                    }
                }

                // Unable to output to file. Don't move to next activity
                Toast toast = Toast.makeText(Main.this, R.string.unable_to_create_file, Toast.LENGTH_LONG);
                toast.show();
            }

            actionMode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(android.support.v7.view.ActionMode actionMode) {
            int i = 10;
            actionMode.finish();
        }

    }

    android.support.v7.view.ActionMode actionMode;
    DownloadablePQ actionModePq;











    /**
     * The fragment listing the DownloadablePQ has been clicked on
     * We open popup bar at top to allow DownloadablePQ to be downloaded
     */
    @Override
    public void onPQClicked(DownloadablePQ pq) {

        //if (actionMode != null) {
            // DownloadablePQ was clicked whilst bar was open for a previous DownloadablePQ click
            // Must manually close previous one
            // (Attempting to immediately open another bar on the new pq doesn't seem to work ?)
        //    actionMode.finish();
         //   actionMode = null;
       // } else {

            // Open top bar to allow selection for DownloadablePQ download
            if (serviceStatus == ServiceStatus.Connected) {
                actionMode = startSupportActionMode(new PopupBar(pq));
                actionModePq = pq;
            }
        //}
    }

    /**
     * The fragment listing the RepeatablePQ has been clicked on
     * We open popup bar at top to allow RepeatablePQ to be downloaded
     */
    @Override
    public void onSchedulePQ(String url) {
        Intent intent = new Intent(this, PQService.class);
        intent.putExtra("operation", PQService.OPERATION_REFRESH);
        intent.putExtra("url", url);
        startService(intent);
    }

    @Override
    public void onServiceRetrievePQList(RetrievePQListResult pqListResult) {
        PQListFragment pqList = (PQListFragment) getFragmentManager().findFragmentById(R.id.pq_list_fragment);

        pqList.updateList(pqListResult.pqs, pqListResult.repeatables);

        if (pqListResult.failure != null) {
            MyDialogFragment dialog = MyDialogFragment.newInstance("Failed", pqListResult.failure.toString());
            dialog.show(getFragmentManager(), "dialog");
        }

        checkActionBar();
    }

    @Override
    public void onServiceProgressInfo(ProgressInfo progressInfo) {
        Assert.assertNotNull(progressInfo);

        ProgressBoxFragment box = getProgressBoxFragment();

        setProgressBarVisibility(true);
        int a = (Window.PROGRESS_END - Window.PROGRESS_START) / 100 * progressInfo.percent;
        setProgress(a);
        checkActionBar();

        box.setText(progressInfo.htmlMessage);
    }


    private void checkActionBar() {
        if (serviceStatus != calculateServiceStatus()) {
            serviceStatus = calculateServiceStatus();

            invalidateOptionsMenu();

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
        ProgressBoxFragment box = (ProgressBoxFragment) getFragmentManager().findFragmentByTag("robtag");

        if (box != null)
            return box;

        // Fragment is not currently up. Add it
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        box = new ProgressBoxFragment();
        fragmentTransaction.add(R.id.fragment_content, box, "robtag");
        fragmentTransaction.commit();

        return box;
    }

    private void hideProgressBoxFragment() {
        ProgressBoxFragment box = (ProgressBoxFragment) getFragmentManager().findFragmentByTag("robtag");

        if (box == null)
            return;

        // Fragment is up, need to remove it
        FragmentManager fragmentManager = getFragmentManager();
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
                                         int notificationId, File fileNameDownloaded) {

        MyDialogFragment dialog = MyDialogFragment.newInstance(title, message, fileNameDownloaded);
        dialog.show(getFragmentManager(), "dialog");

        checkActionBar();

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(notificationId);
    }


    /**
     * Called asynchronously by OS in response to our permissions request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (canCreateFile()) {
            if (requestCode == CREATE_REQUEST_CODE) {
                startActivity(new Intent(this, CreateFiltersActivity.class));
            }
            if (requestCode == DOWNLOAD_REQUEST_CODE) {
                Intent intent = new Intent(this, PQService.class);
                intent.putExtra("operation", PQService.OPERATION_DOWNLOAD);
                intent.putExtra("pq", (Parcelable) actionModePq);
                startService(intent);
            }
        } else {
            // Unable to output to file. This should block moving on to next activity
            Toast toast = Toast.makeText(this, R.string.unable_to_create_file, Toast.LENGTH_LONG);
            toast.show();
        }

    }



    /**
     * Check if can write to the configured (or default) output directory
     * @return true if can write file now (if false. Possible could be true later if permissions given)
     */
    private boolean canCreateFile() {

        if (!Prefs.getDownload(this)) {
            return true;        // aren't going to download PQ so don't worry
        }

        // Build output file
        String dir = Util.getDefaultDownloadDirectory(this);
        if (!Prefs.isDefaultDownloadDir(this)) {
            dir = Prefs.getUserSpecifiedDownloadDir(this);
        }
        File outputDirectory = new File(dir);

        // Check exists
        if (outputDirectory.mkdirs() || outputDirectory.isDirectory()) {

            boolean b = outputDirectory.canWrite();

            if (b) {
                // All good. Can silently return true;
                return true;
            }
        }

        // Unable to write to file now
        return false;
    }

}

/* 
 * Copyright (C) 2011 Robert Neild
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.pquery;

import org.pquery.R;
import org.pquery.service.DownloadPQResult;
import org.pquery.service.PQService;
import org.pquery.service.PQServiceListener;
import org.pquery.service.RetrievePQListResult;
import org.pquery.util.Logger;
import org.pquery.webdriver.ProgressInfo;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.widget.ProgressBar;
import android.widget.TextView;

//public class Detail extends SherlockActivity implements PQServiceListener {
//
//    private static final String PROGRESS_TEXT_BUNDLE = "ProgressTextBundle";
//    
//    /**
//     * Show login progress
//     */
//    private ProgressBar bar;
//    private ProgressBar progressSpinner;
//    private TextView progressText;
//
//    private enum ServiceStatus { NotConnected, Connected, ServiceBusy };
//    
//    private ServiceStatus serviceStatus;
//    
//    private PQService service;
//    
//    
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        Logger.d("enter");
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        setContentView(R.layout.detail);
//
//        // Store references to controls
//
//        bar = (ProgressBar) findViewById(R.id.progress_bar);
//        progressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
//        progressText = (TextView) findViewById(R.id.progress_text);
//
//        if (savedInstanceState != null && savedInstanceState.getCharSequence(PROGRESS_TEXT_BUNDLE) != null) {
//            
//            progressText.setText(savedInstanceState.getCharSequence(PROGRESS_TEXT_BUNDLE));
//            
//        } else {
//            // This Activity can be launched from main menu or notification
//            // If notification, then info (about notification) is passed in
//            //
//            // As service maybe stopped by now (and we can no longer get latest
//            // info
//            // from it)
//            // we need initialise display with notification info
//
//            Bundle bundle = getIntent().getBundleExtra("resultInfo");
//            if (bundle != null) {
////                // Send ourselves fake message from to initialise display with
////                // stuff passed in with intent
////                Logger.d("Got passed in bundle");
////                Message m = Message.obtain(null, PQService.MSG_UPDATE, ResultInfo.createFromBundle(bundle));
////                try {
////                    messengerIn.send(m);
////                } catch (RemoteException e) {
////                }
//            }
//        }
//    }
//    
//    @Override
//    protected void onStart() {
//        super.onStart();
//        doBindService();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        doUnbindService();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        if (serviceStatus == ServiceStatus.ServiceBusy) {
//            menu.add(0, R.string.cancel, 0, R.string.cancel)
//            .setIcon(R.drawable.navigation_cancel)
//            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
//        }
//        return true;
//    }
//    
//    
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//        case android.R.id.home:
//            Intent intent = new Intent(this, Main.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            startActivity(intent);
//            return true;
//        case R.string.cancel:
//            AlertDialog.Builder builder = new AlertDialog.Builder(Detail.this);
//            builder.setMessage("Are you sure?");
//            builder.setPositiveButton(android.R.string.yes, new OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    if (service!=null)
//                        service.cancelInProgress();
//                }
//            });
//            builder.setNegativeButton(android.R.string.no, new OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                }
//            });
//            builder.show();
//        default:
//            return super.onOptionsItemSelected(item);
//        }
//    }
//    
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        Logger.d("saving state " + progressText.getText());
//        outState.putCharSequence(PROGRESS_TEXT_BUNDLE, progressText.getText());
//    }
//
//
//    
//    
//
//    private boolean isServiceBound;
//
//    private ServiceConnection connection = new ServiceConnection() {
//
//        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
//            service = ((PQService.LocalBinder)serviceBinder).getService();
//            service.registerClient(Detail.this);
//            
//            checkActionBar();
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            Logger.d("enter");
//            service = null;
//            
//            checkActionBar();
//        }
//    };
//
//    /**
//     * Register interest in service We will get ServiceConnection callbacks when
//     * service starts/stops
//     */
//    private void doBindService() {
//        isServiceBound = bindService(new Intent(getApplicationContext(), PQService.class), connection, 0);
//    }
//
//    /**
//     * Stop being interested in service If still connected to service, sent
//     * unregister message to it
//     */
//    private void doUnbindService() {
//        if (isServiceBound) {
//            if (service!=null)
//                service.unRegisterClient(this);
//            
//            unbindService(connection);
//            isServiceBound = false;
//        }
//
//        service = null;
//    }
//
//    
//    @Override
//    public void onServiceProgressInfo(ProgressInfo progressInfo) {
//        
//        bar.setProgress(progressInfo.percent);
//        progressText.setText(Html.fromHtml(progressInfo.htmlMessage));
//        
//        checkActionBar();
//    }
//    
//    private void checkActionBar() {
//        if (serviceStatus != calculateServiceStatus()) {
//            serviceStatus = calculateServiceStatus();
//            
//            supportInvalidateOptionsMenu();
//        }
//    }
//    
//    private ServiceStatus calculateServiceStatus() {
//        if (service == null)
//            return ServiceStatus.NotConnected;
//        if (service.isOperationInProgress())
//            return ServiceStatus.ServiceBusy;
//        return ServiceStatus.Connected;
//    }
//
//    @Override
//    public void onServicePQDownloaded(DownloadPQResult downloadPQResult) {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public void onServiceRetrievePQList(RetrievePQListResult pqListResult) {
//        // TODO Auto-generated method stub
//        
//    }
//}
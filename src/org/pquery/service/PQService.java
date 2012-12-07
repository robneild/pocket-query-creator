package org.pquery.service;

import java.util.ArrayList;
import java.util.concurrent.CancellationException;

import junit.framework.Assert;

import org.pquery.Main;
import org.pquery.QueryStore;
import org.pquery.dao.PQ;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.webdriver.ProgressInfo;
import org.pquery.webdriver.ResultInfo;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Service that runs long running network tasks
 */
public class PQService extends Service {

    public static final int OPERATION_REFRESH=1;
    public static final int OPERATION_DOWNLOAD=2;
    public static final int OPERATION_CREATE=3;

    private NotificationUtil notificationUtil;

    private RetrievePQListAsync retrievePQListAsync;
    private DownloadPQAsync downloadPQAsync;
    private CreatePQAsync createPQAsync;
    
    /** Keeps track of all current registered clients. */
    private ArrayList<PQServiceListener> clients = new ArrayList<PQServiceListener>();

    /** Store the last service update */
    private ProgressInfo lastUpdate;

    public void registerClient(PQServiceListener client) {
        clients.add(client);
        if (isOperationInProgress())
            sendMessageToClients(lastUpdate);
    }
    public void unRegisterClient(PQServiceListener client) {
        clients.remove(client);
    }
    public boolean isOperationInProgress() {
        if (retrievePQListAsync!=null || downloadPQAsync!=null || createPQAsync!=null)
            return true;
        return false;
    }
    public void cancelInProgress() {
        if (retrievePQListAsync!=null) {
            retrievePQListAsync.cancel(true);
            retrievePQListAsync=null;
        }
        if (downloadPQAsync!=null) {
            downloadPQAsync.cancel(true);
            downloadPQAsync=null;
        }
        if (createPQAsync!=null) {
            createPQAsync.cancel(true);
            createPQAsync=null;
        }
    }
    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public PQService getService() {
            return PQService.this;
        }
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clients.clear();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationUtil = new NotificationUtil(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("enter [flags="+flags+"]");

        Bundle extras = intent.getExtras();
        int operation = intent.getIntExtra("operation", 0);
        
        for (PQServiceListener client : clients) {
            client.onServiceStartingTask();
        }
        
        switch (operation) {
        case OPERATION_REFRESH:
            handleRetrievePQList(extras);
            break;
        case OPERATION_DOWNLOAD:
            handlePQDownload(extras);
            break;
        case OPERATION_CREATE:
            handlePQCreation(extras);
            break;
        default:
            Assert.assertFalse(true);
        }



        //          notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //
        //        // Extract creation info from bundle
        //        Bundle bundle = intent.getBundleExtra("QueryStore");
        //        Assert.assertNotNull(bundle);
        //        QueryStore queryStore = new QueryStore(bundle);
        //
        //        // Kick off background thread
        //
        //        async = new CreateAsync(true, getApplicationContext(), queryStore,
        //                (LocationManager) getSystemService(LOCATION_SERVICE)) {
        //
        //            @Override
        //            protected void onPostExecute(ResultInfo result) {
        //                super.onPostExecute(result);
        //                stopSelf();
        //                Logger.d("exit");
        //            };
        //
        //            @Override
        //            protected void onProgressUpdate(ProgressInfo... values) {
        //                sendMessageToClients(values[0]);
        //                Logger.d("" + values[0]);
        //            }
        //
        //        };
        //        async.execute();

        Logger.d("returning");
        // If service gets killed due to low memory we don't want it auto re-launched
        // It would be too difficult to resume properly
        return START_NOT_STICKY;
    }

    private void handlePQCreation(Bundle extras) {

        final QueryStore queryStore = new QueryStore(extras.getBundle("queryStore"));

        final int notificationId = notificationUtil.createNotification("Creating PQ", "", getPendingIntent());

        // Kick off background thread

        createPQAsync = new CreatePQAsync(getApplicationContext(), queryStore, (LocationManager) getSystemService(LOCATION_SERVICE)) {

            @Override
            protected void onPostExecute(CreatePQResult result) {
                super.onPostExecute(result);
                String title = result.getTitle();
                String message = result.getMessage();
                notificationUtil.changeNotification(notificationId, title, message, getPendingIntent(title,message,notificationId));
                cleanUpAndStopSelf();
                sendMessageToClients(result);
            }
            
            @Override
            protected void onCancelled() {
                notificationUtil.dismissNotification(notificationId);
                cleanUpAndStopSelf();
            }

            @Override
            protected void onProgressUpdate(ProgressInfo... values) {
                sendMessageToClients(values[0]);
                Logger.d("" + values[0]);
            }
        };
        createPQAsync.execute();
    }

    private void handlePQDownload(Bundle extras) {

        final PQ pq = (PQ) extras.get("pq");

        Assert.assertNotNull(pq);
        Assert.assertNull(downloadPQAsync);

        final int notificationId = notificationUtil.createNotification("Downloading PQ", "", getPendingIntent());

        downloadPQAsync = new DownloadPQAsync(getApplicationContext(), pq) {

            @Override
            protected void onPostExecute(DownloadPQResult result) {
                super.onPostExecute(result);
                String title = result.getTitle();
                String message = result.getMessage();
                notificationUtil.changeNotification(notificationId, title, message, getPendingIntent(title,message,notificationId));
                cleanUpAndStopSelf();
                sendMessageToClients(result);
            }

            @Override
            protected void onCancelled() {
                notificationUtil.dismissNotification(notificationId);
                cleanUpAndStopSelf();
            }

            @Override
            protected void onProgressUpdate(ProgressInfo... values) {
                sendMessageToClients(values[0]);
                Logger.d("" + values[0]);
            }
        };
        downloadPQAsync.execute();
    }



    private void handleRetrievePQList(final Bundle extras) {

        Assert.assertNull(retrievePQListAsync);

        retrievePQListAsync = new RetrievePQListAsync(getApplicationContext()) {

            @Override
            protected void onCancelled() {
                cleanUpAndStopSelf();
            }

            @Override
            protected void onPostExecute(RetrievePQListResult result) {
                super.onPostExecute(result);
                cleanUpAndStopSelf();

                Prefs.savePQListState(PQService.this, result.pqs);
                sendMessageToClients(result);
            }

            @Override
            protected void onProgressUpdate(ProgressInfo... values) {
                sendMessageToClients(values[0]);
                Logger.d("" + values[0]);
            }

        };
        retrievePQListAsync.execute();
    }

    private PendingIntent getPendingIntent(String title, String message, int notificationId) {
        Intent intent = new Intent(getApplicationContext(), Main.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("notificationId", notificationId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }


    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(getApplicationContext(), Main.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    //    private void handleFailure(FailureResult failureResult) {
    //
    //        String fail = getResources().getString(R.string.service_creation_failed);
    //
    //        Notification notification = new Notification(R.drawable.status_bar2, fail,
    //                System.currentTimeMillis());
    //        notification.setLatestEventInfo(getApplicationContext(), fail, failureResult.failMessage,
    //                getPendingIntent());
    //        notification.defaults = Notification.DEFAULT_ALL;      // vibrate etc
    //        notifManager.notify(NOTIFICATION_ID, notification);
    //    }

    private void sendMessageToClients(ProgressInfo progress) {
        lastUpdate = progress;
        for (PQServiceListener client : clients) {
            client.onServiceProgressInfo(progress);
        }
    }

    private void sendMessageToClients(DownloadPQResult down) {
        //lastUpdate = value;
        for (PQServiceListener client : clients) {
            client.onServicePQDownloaded(down);
        }
    }

    private void sendMessageToClients(CreatePQResult result) {
        for (PQServiceListener client : clients) {
            client.onServicePQCreated(result);
        }
    }
    
    private void sendMessageToClients(RetrievePQListResult list) {
        //lastUpdate = value;
        for (PQServiceListener client : clients) {
            client.onServiceRetrievePQList(list);
        }
    }

    private void cleanUpAndStopSelf() {
        downloadPQAsync = null;
        retrievePQListAsync = null;
        createPQAsync = null;
        
        for (PQServiceListener client : clients) {
            client.onServiceStoppedTask();
        }
        stopSelf();
    }

}

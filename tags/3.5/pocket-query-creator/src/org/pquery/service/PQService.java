package org.pquery.service;

import java.util.ArrayList;
import junit.framework.Assert;

import org.pquery.Main;
import org.pquery.QueryStore;
import org.pquery.dao.PQ;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.webdriver.ProgressInfo;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;

/**
 * Service that runs long running network tasks
 */
public class PQService extends Service {

    public static final int OPERATION_REFRESH=1;
    public static final int OPERATION_DOWNLOAD=2;
    public static final int OPERATION_CREATE=3;

    private NotificationUtil notificationUtil;
    private PowerManager.WakeLock wakeLock;
    
    private RetrievePQListAsync retrievePQListAsync;
    private DownloadPQAsync downloadPQAsync;
    private CreatePQAsync createPQAsync;
    
    /** Keeps track of all current registered clients. */
    private ArrayList<PQServiceListener> clients = new ArrayList<PQServiceListener>();

    /** 
     * Store the last service update we sent to clients
     */
    private ProgressInfo lastUpdate;

    public void registerClient(PQServiceListener client) {
        clients.add(client);
        if (isOperationInProgress())
        	if (lastUpdate!=null)
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
        	Logger.d("Cancelling retrievePQListAsync");
            retrievePQListAsync.cancel(true);
        } else if (downloadPQAsync!=null) {
        	Logger.d("Cancelling downloadPQAsync");
            downloadPQAsync.cancel(true);
        } else if (createPQAsync!=null) {
        	Logger.d("Cancelling createPQAsync");
            createPQAsync.cancel(true);
        } else {
        	Assert.assertTrue(false);
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
        Logger.d("destroy");
        clients.clear();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("create");
        notificationUtil = new NotificationUtil(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle extras = intent.getExtras();
        int operation = intent.getIntExtra("operation", 0);
        
        Logger.d("enter [flags="+flags+",operation="+operation+",startId="+startId+"]");

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

        Logger.d("returning");
        // If service gets killed due to low memory we don't want it auto re-launched
        // It would be too difficult to resume properly
        return START_NOT_STICKY;
    }

    private void handlePQCreation(Bundle extras) {

        final QueryStore queryStore = new QueryStore(extras.getBundle("queryStore"));
        
        Assert.assertNull(wakeLock);
        Assert.assertNull(createPQAsync);
        
        wakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"PocketQuery");
        wakeLock.acquire();
        notificationUtil.startInProgressNotification("Creating PQ", "", getPendingIntent());

        // Kick off background thread

        createPQAsync = new CreatePQAsync(getApplicationContext(), queryStore, (LocationManager) getSystemService(LOCATION_SERVICE)) {

            @Override
            protected void onPostExecute(CreatePQResult result) {
                super.onPostExecute(result);
                String title = result.getTitle();
                String message = result.getMessage();
                int notificationId = notificationUtil.showEndNotification(title, message);
                
                Prefs.erasePQListState(PQService.this);		// erase any PQ list as we know is out-of-date now
                sendMessageToClients(new RetrievePQListResult());		// sends an empty PQ list to GUI so will redraw empty
                sendMessageToClients(title, message, notificationId);
                cleanUpAndStopSelf();
            }
            
            @Override
            protected void onCancelled() {
                notificationUtil.closeInProgressNotification();
                cleanUpAndStopSelf();
            }

            @Override
            protected void onProgressUpdate(ProgressInfo... values) {
            	if (!isCancelled()) {
            		sendMessageToClients(values[0]);
            		Logger.d("" + values[0]);
            	}
            }
        };
        createPQAsync.execute();
    }

    private void handlePQDownload(Bundle extras) {

        final PQ pq = (PQ) extras.get("pq");

        Assert.assertNotNull(pq);
        Assert.assertNull(downloadPQAsync);
        Assert.assertNull(wakeLock);
        
        wakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"PocketQuery");
        wakeLock.acquire();
        notificationUtil.startInProgressNotification("Downloading PQ " + pq.name, "", getPendingIntent());

        downloadPQAsync = new DownloadPQAsync(getApplicationContext(), pq) {

            @Override
            protected void onPostExecute(DownloadPQResult result) {
                super.onPostExecute(result);
                String title = result.getTitle();
                String message = result.getMessage();
                int notificationId = notificationUtil.showEndNotification(title, message);
                
                sendMessageToClients(title, message, notificationId);
                cleanUpAndStopSelf();
            }

            @Override
            protected void onCancelled() {
                notificationUtil.closeInProgressNotification();
                cleanUpAndStopSelf();
            }

            @Override
            protected void onProgressUpdate(ProgressInfo... values) {
            	if (!isCancelled()) {
            		sendMessageToClients(values[0]);
            		Logger.d("" + values[0]);
            	}
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
                
                Prefs.savePQListState(PQService.this, result.pqs);
                sendMessageToClients(result);
                
                cleanUpAndStopSelf();
            }

            @Override
            protected void onProgressUpdate(ProgressInfo... values) {
            	Assert.assertNotNull(values[0]);
            	
            	if (!isCancelled()) {
            		sendMessageToClients(values[0]);
                	Logger.d("" + values[0]);
            	}
            }

        };
        retrievePQListAsync.execute();
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(getApplicationContext(), Main.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void sendMessageToClients(ProgressInfo progress) {
        lastUpdate = progress;
        for (PQServiceListener client : clients) {
            client.onServiceProgressInfo(progress);
        }
    }

    private void sendMessageToClients(String title, String message, int notificationId) {
        //lastUpdate = value;
        for (PQServiceListener client : clients) {
            client.onServiceOperationResult(title, message, notificationId);
        }
    }
    
//    
//    private void sendMessageToClients(DownloadPQResult down) {
//        //lastUpdate = value;
//        for (PQServiceListener client : clients) {
//            client.onServicePQDownloaded(down);
//        }
//    }
//
//    private void sendMessageToClients(CreatePQResult result) {
//        for (PQServiceListener client : clients) {
//            client.onServicePQCreated(result);
//        }
//    }
    
    private void sendMessageToClients(RetrievePQListResult list) {
        //lastUpdate = value;
        for (PQServiceListener client : clients) {
            client.onServiceRetrievePQList(list);
        }
    }

    private void cleanUpAndStopSelf() {
    	Logger.d("stopping");
    	
        downloadPQAsync = null;
        retrievePQListAsync = null;
        createPQAsync = null;
        
        lastUpdate = null;
        
        for (PQServiceListener client : clients) {
            client.onServiceStoppedTask();
        }
        
        if (wakeLock!=null)
        	wakeLock.release();
        wakeLock = null;
        
        stopSelf();
    }

}

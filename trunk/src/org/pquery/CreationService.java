//package org.pquery;
//
//import java.util.ArrayList;
//
//import junit.framework.Assert;
//
//import org.pquery.service.RetrievePQListAsync;
//import org.pquery.service.RetrievePQListResult;
//import org.pquery.util.Logger;
//import org.pquery.webdriver.FailureResult;
//import org.pquery.webdriver.ProgressInfo;
//import org.pquery.webdriver.ResultInfo;
//import org.pquery.webdriver.SuccessResult;
//
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Intent;
//import android.location.LocationManager;
//import android.os.Binder;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Message;
//import android.os.Messenger;
//import android.os.RemoteException;
//import android.util.Log;
//
///**
// * Service that runs in background and actually connects to geocaching.com and
// * creates the pocket query
// */
//public class CreationService extends Service {
//
//	private NotificationManager mNM;
//
//	//private CreateAsync async;
//	
//	/** Keeps track of all current registered clients. */
//	private ArrayList<Messenger> clients = new ArrayList<Messenger>();
//
//	/** Unique id Number for our Notification */
//	private static final int NOTIFICATION_ID = 10;
//
//	/** Store the last service update */
//	private ResultInfo lastUpdate;
//
//	/**
//	 * Command to the service to register a client, receiving callbacks from the
//	 * service. The Message's replyTo field must be a Messenger of the client
//	 * where callbacks should be sent.
//	 */
//	public static final int MSG_REGISTER_CLIENT = 1;
//
//	/**
//	 * Command to the service to unregister a client, ot stop receiving
//	 * callbacks from the service. The Message's replyTo field must be a
//	 * Messenger of the client as previously given with MSG_REGISTER_CLIENT.
//	 */
//	public static final int MSG_UNREGISTER_CLIENT = 2;
//
//	public static final int MSG_STOP = 3;
//
//	public static final int MSG_UPDATE = 5;
//
//	class IncomingHandler extends Handler {
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case MSG_REGISTER_CLIENT:
//				clients.add(msg.replyTo);
//				if (lastUpdate != null)
//					sendMessageToClients(lastUpdate);
//				break;
//			case MSG_UNREGISTER_CLIENT:
//				clients.remove(msg.replyTo);
//				break;
//			case MSG_STOP:
//			    Logger.d("Received stop message");
//				//async.cancel(true);
//				FailureResult fail = new FailureResult(getString(R.string.service_cancelled));
//				sendMessageToClients(fail);
//				handleFailure(fail);
//				stopSelf();
//				break;
//			default:
//				super.handleMessage(msg);
//			}
//		}
//	}
//
//	final Messenger mMessenger = new Messenger(new IncomingHandler());
//
//	/**
//	 * Class for clients to access. Because we know this service always runs in
//	 * the same process as its clients, we don't need to deal with IPC.
//	 */
//	public class LocalBinder extends Binder {
//		CreationService getService() {
//			return CreationService.this;
//		}
//	}
//
//	@Override
//	public void onCreate() {
//		Logger.d("enter");
//	}
//
//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		Log.i("LocalService", "Received start id " + startId + ": " + intent);
//
//		int operation = intent.getIntExtra("operation", 0);
//		
//		if (operation == 1) {
//		    handleRetrievePQList(intent.getExtras());
//		}
//	
////		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
////
////		// Close any old notifications showing from a previous run
////		mNM.cancel(NOTIFICATION_ID);
////
////		// Extract creation info from bundle
////		Bundle bundle = intent.getBundleExtra("QueryStore");
////		Assert.assertNotNull(bundle);
////		QueryStore queryStore = new QueryStore(bundle);
////
////		// Send out initial update event to client
////		
////		String started = getResources().getString(R.string.local_service_started);
////		sendMessageToClients(new ProgressInfo(0, started));
////		
////		// Start notification
////		
////		Notification notification = new Notification(R.drawable.status_bar2, started,
////				System.currentTimeMillis());
////		notification.setLatestEventInfo(getApplicationContext(), started, "", getPendingIntent());
////		notification.flags = Notification.FLAG_ONGOING_EVENT;
////
////		mNM.notify(NOTIFICATION_ID, notification);
////		
////		// Kick off background thread
////
////		async = new CreateAsync(true, getApplicationContext(), queryStore,
////				(LocationManager) getSystemService(LOCATION_SERVICE)) {
////
////			@Override
////			protected void onPostExecute(ResultInfo result) {
////			    super.onPostExecute(result);
////			    
////			    Logger.d("enter");
////			    
////				sendMessageToClients(result);
////
////				if ((Object) result instanceof FailureResult) {
////
////					handleFailure((FailureResult)result);
////				}
////
////				if ((Object) result instanceof SuccessResult) {
////
////					SuccessResult successResult = (SuccessResult) result;
////					String success = getResources().getString(R.string.service_creation_success);
////
////					Notification notification = new Notification(R.drawable.status_bar2, success,
////							System.currentTimeMillis());
////					notification.setLatestEventInfo(getApplicationContext(), success, successResult.successMessage, getPendingIntent());
////					notification.defaults = Notification.DEFAULT_ALL;      // vibrate etc
////					mNM.notify(NOTIFICATION_ID, notification);
////				}
////
////				stopSelf();
////				Logger.d("exit");
////			};
////
////			@Override
////			protected void onProgressUpdate(ProgressInfo... values) {
////				sendMessageToClients(values[0]);
////				Logger.d("" + values[0]);
////			}
////
////		};
////		async.execute();
//
//		
//		
//		Logger.d("returning");
//		// If service gets killed due to low memory we don't want it auto re-launched
//		// It would be too difficult to resume properly
//		return START_NOT_STICKY;
//	}
//
//	private void handleRetrievePQList(final Bundle extras) {
//	    RetrievePQListAsync async = new RetrievePQListAsync(getApplicationContext()) {
//	        
//	        @Override
//	            protected void onPostExecute(RetrievePQListResult result) {
//	                super.onPostExecute(result);
//	                
//	                  Messenger messenger = (Messenger) extras.get("messenger");
//	                  Message msg = Message.obtain();
//	                  msg.obj = result;
//	                  try {
//	                    messenger.send(msg);
//	                  } catch (android.os.RemoteException e) {
//	                    Logger.e("Unable to send back message", e);
//	                  }
//	                
//	            }
//
//          @Override
//          protected void onProgressUpdate(ProgressInfo... values) {
//              sendMessageToClients(values[0]);
//              Logger.d("" + values[0]);
//          }
//
//      };
//      async.execute();
//	}
//	
//	
//	@Override
//	public void onDestroy() {
//		//mNM.cancel(NOTIFICATION_STARTED);
//	}
//
//	@Override
//	public IBinder onBind(Intent intent) {
//		return mMessenger.getBinder();
//	}
//
//	private void sendMessageToClients(ResultInfo value) {
//		lastUpdate = value;
//
//		for (int i = clients.size() - 1; i >= 0; i--) {
//			try {
//				clients.get(i).send(Message.obtain(null, MSG_UPDATE, value));
//			} catch (RemoteException e) {
//				// The client is dead. Remove it from the list;
//				// we are going through the list from back to front
//				// so this is safe to do inside the loop.
//				clients.remove(i);
//			}
//		}
//	}
//
//	private PendingIntent getPendingIntent() {
//		Bundle bundle = new Bundle();
//		lastUpdate.saveToBundle(bundle);
//
//		Intent intent = new Intent(getApplicationContext(), CreationProgress.class);
//		intent.putExtra("resultInfo", bundle);
//
//		return PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//	}
//
//	private void handleFailure(FailureResult failureResult) {
//		
//		String fail = getResources().getString(R.string.service_creation_failed);
//
//		Notification notification = new Notification(R.drawable.status_bar2, fail,
//				System.currentTimeMillis());
//		notification.setLatestEventInfo(getApplicationContext(), fail, failureResult.failMessage,
//				getPendingIntent());
//		notification.defaults = Notification.DEFAULT_ALL;      // vibrate etc
//		mNM.notify(NOTIFICATION_ID, notification);
//	}
//}

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
import org.pquery.util.Logger;
import org.pquery.webdriver.FailureResult;
import org.pquery.webdriver.ProgressInfo;
import org.pquery.webdriver.ResultInfo;
import org.pquery.webdriver.SuccessResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CreationProgress extends Activity {

    private static final String PROGRESS_TEXT_BUNDLE = "ProgressTextBundle";
    /**
     * Show login progress
     */
    private ProgressBar bar;
    private ProgressBar progressSpinner;
    private TextView progressText;
    private Button cancelButton;

    /** Messenger for communicating with service. */
    Messenger messengerOut;

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger messengerIn = new Messenger(new IncomingHandler());

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case CreationService.MSG_UPDATE:
                if (msg.obj instanceof ProgressInfo) {
                    updateProgress((ProgressInfo) msg.obj);
                }
                if (msg.obj instanceof FailureResult) {
                    progressText.setText(Html.fromHtml(((FailureResult) msg.obj).failMessageHtml));
                }
                if (msg.obj instanceof SuccessResult) {
                    progressText.setText(Html.fromHtml(((SuccessResult) msg.obj).successMessageHtml));
                }
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d("enter");

        setContentView(R.layout.creation_progress);

        // Store references to controls

        bar = (ProgressBar) findViewById(R.id.progress_bar);
        progressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
        progressText = (TextView) findViewById(R.id.progress_text);
        cancelButton = (Button) findViewById(R.id.button_cancel);

        // Handle cancel button

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(CreationProgress.this);
                builder.setMessage("Are you sure?");
                builder.setPositiveButton(android.R.string.yes, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isServiceBound && messengerOut != null) {
                            Message msg = Message.obtain(null, CreationService.MSG_STOP);
                            try {
                                messengerOut.send(msg);
                            } catch (RemoteException e) {
                                Logger.e(e.getMessage());
                            }
                        }
                    }
                });
                builder.setNegativeButton(android.R.string.no, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        });

        if (savedInstanceState != null && savedInstanceState.getCharSequence(PROGRESS_TEXT_BUNDLE) != null) {
            
            progressText.setText(savedInstanceState.getCharSequence(PROGRESS_TEXT_BUNDLE));
            
        } else {
            // This Activity can be launched from main menu or notification
            // If notification, then info (about notification) is passed in
            //
            // As service maybe stopped by now (and we can no longer get latest
            // info
            // from it)
            // we need initialise display with notification info

            Bundle bundle = getIntent().getBundleExtra("resultInfo");
            if (bundle != null) {
                // Send ourselves fake message from to initialise display with
                // stuff passed in with intent
                Logger.d("Got passed in bundle");
                Message m = Message.obtain(null, CreationService.MSG_UPDATE, ResultInfo.createFromBundle(bundle));
                try {
                    messengerIn.send(m);
                } catch (RemoteException e) {
                }
            }
        }

        doBindService();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.d("saving state " + progressText.getText());
        outState.putCharSequence(PROGRESS_TEXT_BUNDLE, progressText.getText());
    }

    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
    }

    @Override
    protected void onStop() { 
        super.onPause();
        doUnbindService();
    }

    /**
     * Update UI
     */
    void updateProgress(ProgressInfo progress) {
        Logger.d("setting text to " + progress.message);
        bar.setProgress(progress.percent);
        progressText.setText(Html.fromHtml(progress.message));
    }

    private boolean isServiceBound;

    /***
     * Get a callback when service starts and stops
     */
    private ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            messengerOut = new Messenger(service);
            // Register with service. It will auto send us a MSG_UPDATE message
            // with the latest info
            try {
                Message msg = Message.obtain(null, CreationService.MSG_REGISTER_CLIENT);
                msg.replyTo = messengerIn;
                messengerOut.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
            setGuiForRunningService(true);
        }

        public void onServiceDisconnected(ComponentName className) {
            messengerOut = null;
            setGuiForRunningService(false);
        }
    };

    /**
     * Register interest in service We will get ServiceConnection callbacks when
     * service starts/stops
     */
    private void doBindService() {
        isServiceBound = bindService(new Intent(getApplicationContext(), CreationService.class), connection, 0);
    }

    /**
     * Stop being interested in service If still connected to service, sent
     * unregister message to it
     */
    private void doUnbindService() {
        if (isServiceBound) {

            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (messengerOut != null) {
                try {
                    Message msg = Message.obtain(null, CreationService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = messengerIn;
                    messengerOut.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has
                    // crashed.
                    Logger.e(e.getMessage());
                }
            }

            // Detach our existing connection.
            unbindService(connection);
            isServiceBound = false;
        }
        messengerOut = null;
    }

    /**
     * When service is running, add spinner, cancel button etc.
     */
    private void setGuiForRunningService(boolean b) {
        if (b) {
            bar.setVisibility(View.VISIBLE);
            progressSpinner.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        } else {
            bar.setVisibility(View.INVISIBLE);
            progressSpinner.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
        }
    }

}
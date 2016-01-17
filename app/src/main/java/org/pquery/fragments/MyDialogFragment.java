package org.pquery.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

import org.pquery.R;

import java.io.File;
import java.util.List;

public class MyDialogFragment extends SherlockDialogFragment {

    public static MyDialogFragment newInstance(String title, String message) {
        return newInstance(title, message, null);
    }

    public static MyDialogFragment newInstance(String title, String message, File fileNameDownloaded) {
        MyDialogFragment frag = new MyDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        String fileNameDownloadedUri = null;
        if (fileNameDownloaded!=null) {
            fileNameDownloadedUri=Uri.fromFile(fileNameDownloaded).toString();
        }
        args.putString("fileNameDownloadedUri", fileNameDownloadedUri);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = getArguments().getString("title");
        String message = getArguments().getString("message");
        final String fileNameDownloadedUriString = getArguments().getString("fileNameDownloadedUri");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, null);
        if (fileNameDownloadedUriString!=null) {
            builder.setNeutralButton(R.string.open_in_app, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Uri fileNameDownloadUri = Uri.parse(fileNameDownloadedUriString);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(fileNameDownloadUri, "application/zip");
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    PackageManager packageManager = getActivity().getPackageManager();
                    List activities = packageManager.queryIntentActivities(intent,
                            PackageManager.MATCH_DEFAULT_ONLY);
                    if ( activities.size() > 0 ) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), R.string.no_app_installed, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return builder.create();
    }
}
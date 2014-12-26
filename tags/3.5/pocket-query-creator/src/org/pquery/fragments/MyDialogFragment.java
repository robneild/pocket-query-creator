package org.pquery.fragments;

import com.actionbarsherlock.app.SherlockDialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

public class MyDialogFragment extends SherlockDialogFragment {

    public static MyDialogFragment newInstance(String title, String message) {
        MyDialogFragment frag = new MyDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = getArguments().getString("title");
        String message = getArguments().getString("message");
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
        .setTitle(title)
        .setPositiveButton(android.R.string.ok, null);
        return builder.create();
    }
}
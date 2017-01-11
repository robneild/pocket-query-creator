package net.bgreco;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import org.pquery.R;
import org.pquery.util.FileComparator;
import org.pquery.util.Prefs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Copyright (C) 2011 by Brad Greco <brad@bgreco.net>
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class DirectoryPicker extends ListActivity {

    // Keys used to store in extra

    public static final String START_DIR = "startDir";
    public static final String ONLY_DIRS = "onlyDirs";
    public static final String SHOW_HIDDEN = "showHidden";
    public static final String CHOSEN_DIRECTORY = "chosenDir";
    public static final String ASKED_PERM = "askedPerm";

    public static final int PICK_DIRECTORY = 43522432;
    public static final int PICK_CANCELLED = 43522433;

    private File dir;
    private boolean showHidden = false;
    private boolean onlyDirs = true;

    /** Remember so only ask for permissions once. And not keep repeating on rotation etc. */
    private boolean askedForPermissions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        // Open at root. Maybe not ideal ?
        dir = new File("/");

        if (extras != null) {
            String preferredStartDir = extras.getString(START_DIR);
            showHidden = extras.getBoolean(SHOW_HIDDEN, false);
            onlyDirs = extras.getBoolean(ONLY_DIRS, true);
            askedForPermissions = extras.getBoolean(ASKED_PERM, false);

            if (preferredStartDir != null) {
                File startDir = new File(preferredStartDir);
                if (startDir.isDirectory()) {
                    dir = startDir;
                }
            }
        }

        // Restore if rotation etc.
        if (savedInstanceState != null) {
            askedForPermissions = savedInstanceState.getBoolean(ASKED_PERM);
        }


        setContentView(R.layout.directory_chooser_list);

        // Android 6 permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M  && !askedForPermissions) {
            askedForPermissions = true;
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 0);
            }
        }

        // Make current directory visible in ActionBar at top
        setTitle(dir.getAbsolutePath());

        // Cancel Button
        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(PICK_CANCELLED);
                finish();
            }
        });

        // Select Button
        Button btnChoose = (Button) findViewById(R.id.btnChoose);
        String name = dir.getName();
        if (name.length() == 0)
            name = "/";
        btnChoose.setText(getResources().getString(R.string.choose) + " '" + name + "'");
        btnChoose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!dir.canWrite()) {
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, R.string.cant_write_directory, Toast.LENGTH_LONG);
                    toast.show();
                    return;
                } else {
                    returnDir(dir.getAbsolutePath());
                }
            }
        });


        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        if (!dir.canRead()) {
            // User has gone into directory that we can't read
            // We immediately close this activity to return to directory above
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, R.string.could_not_read_folder, Toast.LENGTH_LONG);
            toast.show();
            finish();
            return;
        }


        // We now know can read directory so get list of directories
        final List<File> files = filter(dir.listFiles(), onlyDirs, showHidden);
        String[] names = getNamesForFiles(files);

        setListAdapter(new ArrayAdapter<String>(this, R.layout.directory_chooser_list_item, R.id.fdrowtext, names));


        // Go down a directory when selected by launching new activity
        // Pass parameters down (in intent)
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!files.get(position).isDirectory())
                    return;
                String path = files.get(position).getAbsolutePath();
                Intent intent = new Intent(DirectoryPicker.this, DirectoryPicker.class);
                intent.putExtra(DirectoryPicker.START_DIR, path);
                intent.putExtra(DirectoryPicker.SHOW_HIDDEN, showHidden);
                intent.putExtra(DirectoryPicker.ONLY_DIRS, onlyDirs);
                intent.putExtra(DirectoryPicker.ASKED_PERM, askedForPermissions);
                startActivityForResult(intent, PICK_DIRECTORY);
            }
        });
    }

    /** Called before rotate etc */
    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(DirectoryPicker.ASKED_PERM, askedForPermissions);
    }

    /**
     * Directory was clicked. Go down a directory
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_DIRECTORY && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            String path = (String) extras.get(DirectoryPicker.CHOSEN_DIRECTORY);
            returnDir(path);
        }

        if (resultCode == PICK_CANCELLED) {
            setResult(PICK_CANCELLED);
            finish();
        }
    }


    private void returnDir(String path) {
        Intent result = new Intent();
        result.putExtra(CHOSEN_DIRECTORY, path);
        setResult(RESULT_OK, result);


        Prefs.saveUserSpecifiedDownloadDir(this, path);

        finish();
    }

    /**
     * Filter a list of files
     */
    private List<File> filter(File[] file_list, boolean onlyDirs, boolean showHidden) {
        ArrayList<File> files = new ArrayList<File>();
        for (File file : file_list) {
            if (onlyDirs && !file.isDirectory())
                continue;
            if (!showHidden && file.isHidden())
                continue;
            files.add(file);
        }
        Collections.sort(files, new FileComparator());
        return files;
    }


    /**
     * Convert list of files into list of file names
     */
    private String[] getNamesForFiles(List<File> files) {
        String[] names = new String[files.size()];
        int i = 0;
        for (File file : files) {
            names[i] = file.getName();
            i++;
        }
        return names;
    }


}


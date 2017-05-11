package net.bgreco;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
    private String preferredStartDir;

    /** Remember so only ask for permissions once. And not keep repeating on rotation etc. */
    private boolean askedForPermissions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();


        if (extras != null) {
            preferredStartDir = extras.getString(START_DIR);
            showHidden = extras.getBoolean(SHOW_HIDDEN, false);
            onlyDirs = extras.getBoolean(ONLY_DIRS, true);
            askedForPermissions = extras.getBoolean(ASKED_PERM, false);
        }

        // Restore if rotation etc.
        if (savedInstanceState != null) {
            askedForPermissions = savedInstanceState.getBoolean(ASKED_PERM);
        }


        if (preferredStartDir != null) {
            buildChildDir();
        } else {

            // Android 6 permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!askedForPermissions) {
                    askedForPermissions = true;
                    if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 0);
                    } else {
                        buildFullPermissions();        // already have full permissions
                    }
                } else {
                    // Must be waiting to get permissions back ???
                    // Don't do anything
                }

            // old, pre 6 behaviour
            } else {
                buildFullPermissions();    // full permissions
            }
        }
    }

    /**
     * Start directory listing at root
     */
    private void buildFullPermissions() {
        dir = new File("/");

        _build();
    }

    /***
     * Have to start directory listing in our 'local' directory
     */
    private void buildLimitedPermissions() {
        dir = new File(this.getFilesDir().getAbsolutePath());

        _build();
    }

    /**
     * Don't worry too much about permissions as this is a child directory that user
     * is decending into
     */
    private void buildChildDir() {
        dir = new File(preferredStartDir);
        _build();
    }

    private void _build() {

        setContentView(R.layout.directory_chooser_list);

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
                    Toast toast = Toast.makeText(DirectoryPicker.this, R.string.cant_write_directory, Toast.LENGTH_LONG);
                    toast.show();
                    return;
                } else {
                    returnDir(dir.getAbsolutePath());
                }
            }
        });


        ListView lv = getListView();
        lv.setTextFilterEnabled(true);


        // Read directory contents

        // We have to do some manual tinkering depending on directory we are in
        // Not exactly ideal, but android has changed quite a bit on directory structure/permissions
        // etc

        final List<File> files = new ArrayList<>();

        if (dir.getAbsolutePath().equals("/storage/emulated")) {
            // Special handling for "/storage/emulated" directory that we can't read, but can usually
            // access child directories. So just fake contents. This isn't ideal, maybe other ways?
            // However a user wanted access to "/storage/emulated/0"

            files.add(new File("/storage/emulated/0"));

        } else if (dir.getAbsolutePath().equals("/")) {
            // Special handling for root
            // No longer possible to read root on android 7. So fake it's contents

            // Insert 3 symbolic links (shown in blue)
            files.add(new File(getString(R.string.application_directory)));
            files.add(new File(getString(R.string.external_download_directory)));
            files.add(new File(getString(R.string.external_storage_directory)));

            // Hard code directories
            files.add(new File("/mnt"));
            files.add(new File("/sdcard"));
            files.add(new File("/storage"));


        } else {

            if (!dir.canRead()) {
                // User has gone into directory that we can't read
                // We immediately close this activity to return to directory above

                Toast toast = Toast.makeText(this, R.string.could_not_read_folder, Toast.LENGTH_LONG);
                toast.show();
                finish();
                return;
            }

            List<File> filtered = filter(dir.listFiles(), onlyDirs, showHidden);          // Read directory so get list of directories
            files.addAll(filtered);
        }


        String[] names = getNamesForFiles(files);

        // Setup view of files
        setListAdapter(new ArrayAdapter<String>(this, R.layout.directory_chooser_list_item,  R.id.fdrowtext, names) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // Override the standard ArrayAdapter so we can manually style each line
                // This method is called once for each item in list

                String selectedName = files.get(position).getName();
                View view =  super.getView(position, convertView, parent);

                // Special styling for symbolic links at top. Different icon and blue text
                if (selectedName.equals(getString(R.string.application_directory)) ||
                    selectedName.equals(getString(R.string.external_download_directory)) ||
                    selectedName.equals(getString(R.string.external_storage_directory))) {

                    TextView textView = (TextView) view.findViewById(R.id.fdrowtext);
                    textView.setTypeface(null, Typeface.ITALIC);
                    textView.setTextColor(Color.parseColor("#6495ED"));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

                    ImageView imageView = (ImageView) view.findViewById(R.id.fdrowimage);
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.rodentia_icons_emblem_symbolic_link));
                }

                return view;
            }
        });

        // Add listener to folder list

        // Go down a directory when selected by launching new activity
        // Pass parameters down (in intent)
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                File selected = files.get(position);

                if (selected.getName().equals(getString(R.string.application_directory))) {
                    selected = getFilesDir();
                }
                if (selected.getName().equals(getString(R.string.external_download_directory))) {
                    selected = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                }
                if (selected.getName().equals(getString(R.string.external_storage_directory))) {
                    selected = Environment.getExternalStorageDirectory();
                }

                if (!selected.isDirectory())
                    return;

                String path = selected.getAbsolutePath();
                Intent intent = new Intent(DirectoryPicker.this, DirectoryPicker.class);
                intent.putExtra(DirectoryPicker.START_DIR, path);
                intent.putExtra(DirectoryPicker.SHOW_HIDDEN, showHidden);
                intent.putExtra(DirectoryPicker.ONLY_DIRS, onlyDirs);
                intent.putExtra(DirectoryPicker.ASKED_PERM, askedForPermissions);
                startActivityForResult(intent, PICK_DIRECTORY);
            }
        });
    }


    /** Called asynchronously in response to our permissions request */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults != null && grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
            buildFullPermissions();
        } else {

            Toast toast = Toast.makeText(this, R.string.file_permission_denied, Toast.LENGTH_LONG);
            toast.show();

            buildLimitedPermissions();
        }
    }

    /** Called before rotate etc */
    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(DirectoryPicker.ASKED_PERM, askedForPermissions);
    }

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


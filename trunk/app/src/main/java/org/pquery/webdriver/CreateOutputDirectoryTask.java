package org.pquery.webdriver;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import org.pquery.R;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.util.Util;

import java.io.File;

/**
 * Check if directory exists
 * <p/>
 * If it doesn't try to create it
 */
public class CreateOutputDirectoryTask extends RetriableTask<File> {

    private Context cxt;

    public CreateOutputDirectoryTask(int numberOfRetries, int fromPercent, int toPercent, ProgressListener progressListener, CancelledListener cancelledListener, Context cxt) {
        super(numberOfRetries, fromPercent, toPercent, progressListener, cancelledListener, cxt.getResources());
        this.cxt = cxt;
    }

    @Override
    protected File task() throws FailureException, FailurePermanentException {

        Logger.d("enter");

        // Check for sd-card being mounted
        // If one isn't inserted or it is being accessed by a PC, there isn't
        // much we can do

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            throw new FailureException(res.getString(R.string.external_storage_unavailable));
        }

        boolean error = false;

        // Get the output directory
        // This will usually be sdcard/Downloads but the user can
        // override this in preferences

        String dir = Util.getDefaultDownloadDirectory();
        if (!Prefs.isDefaultDownloadDir(cxt))
            dir = Prefs.getUserSpecifiedDownloadDir(cxt);

        File outputDirectory = new File(dir);

        // Check directory tree exists (even possible that the Downloads directory doesn't exist)

        if (outputDirectory.mkdirs() || outputDirectory.isDirectory()) {
            // everything is ok, directory created or already existed
        } else
            error = true;


        if (error)
            throw new FailureException(res.getString(R.string.file_creation_error), outputDirectory.getAbsolutePath());

        return outputDirectory;
    }

}




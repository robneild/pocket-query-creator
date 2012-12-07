package org.pquery.webdriver;

import java.io.File;
import java.io.IOException;
import org.pquery.R;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;
import org.pquery.util.Util;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

public class CreateOutputFileTask extends RetriableTask<File> {

    private Context cxt;
    private Resources res;
    private String baseName;

    public CreateOutputFileTask(int numberOfRetries, int fromPercent, int toPercent, ProgressListener progressListener, CancelledListener cancelledListener, Context cxt, String baseName) {
        super(numberOfRetries, fromPercent, toPercent, progressListener, cancelledListener);
        this.cxt = cxt;
        this.res = cxt.getResources();
        this.baseName = baseName;
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

        // Does all the work, combines the prefix with basename with the extension
        // and then tries to find a unique file name by adding on (1) etc if necessary
        
        File outputFile = Util.getUniqueFile(dir, Prefs.getDownloadPrefix(cxt) + baseName, (Prefs.isZip(cxt) ? "zip": ""));

        // Check directory tree exists above file (even possible that the Downloads directory doesn't exist)
        
        try {
            if (outputFile.exists())
                error = true;
            new File(outputFile.getParent()).mkdirs();
            if (!outputFile.createNewFile())
                error = true;
        } catch (IOException e) {
            throw new FailureException(res.getString(R.string.file_creation_error), e);
        }

        if (error)
            throw new FailureException(res.getString(R.string.file_creation_error), outputFile.getAbsolutePath());

        return outputFile;
    }

}




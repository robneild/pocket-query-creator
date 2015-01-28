package org.pquery.service;

import android.content.res.Resources;

import org.pquery.R;
import org.pquery.webdriver.FailurePermanentException;

import java.io.File;

public class DownloadPQResult {

    public FailurePermanentException failure;
    public File fileNameDownloaded;

    public DownloadPQResult(FailurePermanentException failure) {
        this.failure = failure;
    }

    public DownloadPQResult(File fileNameDownloaded) {
        this.fileNameDownloaded = fileNameDownloaded;
    }

    public String getTitle(Resources res) {
        if (failure == null)
            return res.getString(R.string.downloaded_pq);
        else
            return res.getString(R.string.download_failed);
    }

    public String getMessage(Resources res) {
        if (failure == null)
            return String.format(res.getString(R.string.pq_downloaded_into), fileNameDownloaded.getAbsolutePath());
        else
            return failure.toString();
    }
}

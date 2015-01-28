package org.pquery.service;

import android.content.res.Resources;

import org.pquery.R;
import org.pquery.webdriver.FailurePermanentException;

public class CreatePQResult {

    public FailurePermanentException failure;
    public DownloadPQResult downloadPQResult;
    public String successMessage;

    public CreatePQResult(FailurePermanentException failure) {
        this.failure = failure;
    }

    public CreatePQResult(String success) {
        this.successMessage = success;
    }

    public CreatePQResult(DownloadPQResult downloadPQResult) {
        this.downloadPQResult = downloadPQResult;
    }


    public String getTitle(Resources res) {
        if (failure != null)
            return res.getString(R.string.create_failed);
        if (downloadPQResult != null)
            return downloadPQResult.getTitle(res);
        return res.getString(R.string.created_ok);
    }

    public String getMessage(Resources res) {
        if (failure != null)
            return failure.toString();
        if (downloadPQResult != null)
            return downloadPQResult.getMessage(res);
        return successMessage;
    }
}

package org.pquery.service;

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
    
    
    public String getTitle() {
        if (failure!=null)
            return "Create failed";
        if (downloadPQResult!=null)
            return downloadPQResult.getTitle();
        return "Created OK";
    }
    
    public String getMessage() {
        if (failure!=null)
            return failure.toString();
        if (downloadPQResult!=null)
            return downloadPQResult.getMessage();
        return successMessage;
    }
}

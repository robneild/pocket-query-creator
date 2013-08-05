package org.pquery.service;

import java.io.File;

import org.pquery.webdriver.FailurePermanentException;

public class DownloadPQResult {
    
    public FailurePermanentException failure;
    public File fileNameDownloaded;
    
    public DownloadPQResult(FailurePermanentException failure) {
        this.failure = failure;
    }
    
    public DownloadPQResult(File fileNameDownloaded) {
        this.fileNameDownloaded = fileNameDownloaded;
    }
    
    public String getTitle() {
        if (failure==null)
            return "PQ Downloaded";
        else
            return "Download failed";
    }
    
    public String getMessage() {
        if (failure==null)
            return "Pocket Query downloaded into "+fileNameDownloaded.getAbsolutePath();
        else
            return failure.toString();
    }
}

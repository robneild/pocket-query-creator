package org.pquery.service;

import org.pquery.webdriver.ProgressInfo;

public interface PQServiceListener {
    
    public void onServicePQDownloaded(DownloadPQResult downloadPQResult);
    public void onServiceRetrievePQList(RetrievePQListResult pqListResult);
    public void onServicePQCreated(CreatePQResult createPQResult);
    
    public void onServiceProgressInfo(ProgressInfo progressInfo);
    
    public void onServiceStartingTask();
    public void onServiceStoppedTask();
}

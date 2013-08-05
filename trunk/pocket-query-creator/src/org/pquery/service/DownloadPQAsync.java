package org.pquery.service;

import java.io.File;

import org.pquery.dao.PQ;
import org.pquery.util.Prefs;
import org.pquery.webdriver.CancelledListener;
import org.pquery.webdriver.CreateOutputDirectoryTask;
import org.pquery.webdriver.DownloadTask;
import org.pquery.webdriver.FailurePermanentException;
import org.pquery.webdriver.ProgressInfo;
import org.pquery.webdriver.ProgressListener;
import android.content.Context;
import android.os.AsyncTask;

public class DownloadPQAsync extends AsyncTask<Void, ProgressInfo, DownloadPQResult> implements CancelledListener, ProgressListener {

    private Context cxt;
    
    /** Details of pocket query we are going to download */
    private PQ pq;
    
    public DownloadPQAsync(Context cxt, PQ pq) {
        this.cxt = cxt;
        this.pq = pq;
    }
    
    @Override
    public void progressReport(ProgressInfo progress) {
        publishProgress(new ProgressInfo[] { progress });
    }

    @Override
    public void ifCancelledThrow() throws InterruptedException {
       if (isCancelled())
           throw new InterruptedException();
    }

    @Override
    protected DownloadPQResult doInBackground(Void... params) {
        try
        {
            // Unlike the PQ creation, for this operation don't bother to retry on 
            // errors
            int retryCount = 0;
            
            CreateOutputDirectoryTask createTask = new CreateOutputDirectoryTask(retryCount, 0, 5, this, this, cxt);
            File outputDirectory = createTask.call();
            
            DownloadTask downloadTask = new DownloadTask(retryCount, 5, 100, this, this, cxt, pq.url, outputDirectory, Prefs.getDownloadPrefix(cxt)+pq.name+".zip");
            File fileDownloaded = downloadTask.call();

            return new DownloadPQResult(fileDownloaded);

        } catch (InterruptedException e) {
            return null;
        } catch (FailurePermanentException e) {
            return new DownloadPQResult(e);
        }
    }
}

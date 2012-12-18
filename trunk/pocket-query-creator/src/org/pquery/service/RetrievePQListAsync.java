package org.pquery.service;

import net.htmlparser.jericho.Source;

import org.pquery.dao.PQ;
import org.pquery.util.Logger;
import org.pquery.webdriver.CancelledListener;
import org.pquery.webdriver.FailurePermanentException;
import org.pquery.webdriver.ProgressInfo;
import org.pquery.webdriver.ProgressListener;
import org.pquery.webdriver.RetrievePageTask;
import org.pquery.webdriver.parser.ParseException;
import org.pquery.webdriver.parser.PocketQueryPage;

import android.content.Context;
import android.os.AsyncTask;

public class RetrievePQListAsync extends AsyncTask<Void, ProgressInfo, RetrievePQListResult> implements CancelledListener, ProgressListener {

    private Context cxt;

    public RetrievePQListAsync(Context cxt) {
        this.cxt = cxt;
    }


    @Override
    protected RetrievePQListResult doInBackground(Void... params) {
        try
        {
            Logger.d("start");
            
            // Unlike the PQ creation, for this operation don't bother to retry on 
            // errors
            int retryCount = 0;
            
            RetrievePageTask task = new RetrievePageTask(retryCount,0,100, this, this, cxt, "/pocket/default.aspx");
            Source parsedHtml = task.call();

            PocketQueryPage queryListPage = new PocketQueryPage(parsedHtml);

            PQ[] pqs = queryListPage.getReadyForDownload();

            return new RetrievePQListResult(pqs);

        } catch (InterruptedException e) {
            return new RetrievePQListResult();
        } catch (FailurePermanentException e) {
            return new RetrievePQListResult(e);
        } catch (ParseException e) {
        	return new RetrievePQListResult(new FailurePermanentException(e.getMessage()));
		}

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
}

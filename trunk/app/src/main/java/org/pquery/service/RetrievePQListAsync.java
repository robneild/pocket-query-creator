package org.pquery.service;

import android.content.Context;
import android.os.AsyncTask;

import net.htmlparser.jericho.Source;

import org.pquery.dao.DownloadablePQ;
import org.pquery.dao.RepeatablePQ;
import org.pquery.util.Logger;
import org.pquery.webdriver.CancelledListener;
import org.pquery.webdriver.FailurePermanentException;
import org.pquery.webdriver.ProgressInfo;
import org.pquery.webdriver.ProgressListener;
import org.pquery.webdriver.RetrievePageTask;
import org.pquery.webdriver.parser.ParseException;
import org.pquery.webdriver.parser.PocketQueryPage;

public class RetrievePQListAsync extends AsyncTask<Void, ProgressInfo, RetrievePQListResult> implements CancelledListener, ProgressListener {

    private Context cxt;
    private String url = "/pocket/default.aspx";

    public RetrievePQListAsync(Context cxt, String url) {
        this.cxt = cxt;
        if (url != null) {
            this.url = url;
        }
    }

    @Override
    protected RetrievePQListResult doInBackground(Void... params) {
        try {
            Logger.d("start");

            // Unlike the DownloadablePQ creation, for this operation don't bother to retry on
            // errors
            int retryCount = 0;

            RetrievePageTask task = new RetrievePageTask(retryCount, 0, 100, this, this, cxt, url);
            Source parsedHtml = task.call();

            PocketQueryPage queryListPage = new PocketQueryPage(parsedHtml);

            DownloadablePQ[] pqs = queryListPage.getReadyForDownload();
            RepeatablePQ[] repeatables = queryListPage.getRepeatables();

            return new RetrievePQListResult(pqs, repeatables);

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
        publishProgress(new ProgressInfo[]{progress});
    }

    @Override
    public void ifCancelledThrow() throws InterruptedException {
        if (isCancelled())
            throw new InterruptedException();
    }
}

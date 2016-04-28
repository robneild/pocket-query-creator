package org.pquery.webdriver;

import android.content.Context;
import android.util.Pair;

import org.pquery.R;
import org.pquery.util.IOUtils;
import org.pquery.util.IOUtils.Listener;
import org.pquery.util.Logger;
import org.pquery.util.Util;

import java.io.IOException;
import java.util.List;

public class SubmitFormPageTask extends RetriableTask<String> {

    private Context cxt;
    private String urlPath;
    private List<Pair<String,String>> form;

    public SubmitFormPageTask(List<Pair<String,String>> form, int numberOfRetries, int fromPercent, int toPercent, ProgressListener progressListener, CancelledListener cancelledListener, Context cxt, String urlPath) {
        super(numberOfRetries, fromPercent, toPercent, progressListener, cancelledListener, cxt.getResources());
        this.cxt = cxt;
        this.urlPath = urlPath;
        this.form = form;
    }

    @Override
    protected String task() throws FailureException, FailurePermanentException, InterruptedException {

        Logger.d("enter");

        String html = "";

        // Create client
        // ASSUMING already logged in at this stage

            // Initialize to 0%

            progressReport(0, res.getString(R.string.creating), res.getString(R.string.submitting));

            try {
                // https://www.geocaching.com/login/default.aspx?redir=%2fpocket%2fdefault.aspx%3f

                html = IOUtils.httpPost(cxt, form, urlPath, false, cancelledListener, new Listener() {

                    @Override
                    public void update(int bytesReadSoFar, int expectedLength, int percent0to100) {
                        progressReport(
                                percent0to100,
                                res.getString(R.string.creating),
                                Util.humanDownloadCounter(bytesReadSoFar, expectedLength));
                    }
                });


            } catch (IOException e) {
                throw new FailureException(res.getString(R.string.unable_to_submit_creation_form), e);
            }

            return html;

    }


}




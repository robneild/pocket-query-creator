package org.pquery.service;

import org.pquery.dao.DownloadablePQ;
import org.pquery.dao.RepeatablePQ;
import org.pquery.webdriver.FailurePermanentException;

public class RetrievePQListResult {
    public FailurePermanentException failure;
    public DownloadablePQ[] pqs = null;
    public RepeatablePQ[] repeatables = null;

    public RetrievePQListResult(FailurePermanentException failure) {
        this.failure = failure;
    }

    public RetrievePQListResult(DownloadablePQ[] pqs, RepeatablePQ[] repeatables) {
        this.pqs = pqs;
        this.repeatables = repeatables;
    }

    public RetrievePQListResult() {
    }
}

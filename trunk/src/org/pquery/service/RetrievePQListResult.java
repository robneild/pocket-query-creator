package org.pquery.service;

import org.pquery.dao.PQ;
import org.pquery.webdriver.FailurePermanentException;

public class RetrievePQListResult {
    public FailurePermanentException failure;
    public PQ[] pqs = null;
    
    public RetrievePQListResult(FailurePermanentException failure) {
        this.failure = failure;
    }
    public RetrievePQListResult(PQ[] pqs) {
        this.pqs = pqs;
    }
    public RetrievePQListResult() {
    }
}

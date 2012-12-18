package org.pquery.webdriver;

import org.pquery.util.Logger;

import junit.framework.Assert;

public abstract class RetriableTask<T> {

    private int numberOfTriesLeft; // number left
    private int lastPercent;
    private ProgressListener progressListener;
    protected CancelledListener cancelledListener;
    private int fromPercent;
    private int toPercent;
    
    public RetriableTask(int numberOfRetries, int fromPercent, int toPercent, ProgressListener progressListener, CancelledListener cancelledListener) {
        Assert.assertNotNull(progressListener);
        Assert.assertNotNull(cancelledListener);
        Assert.assertTrue(toPercent>fromPercent);
        
        this.numberOfTriesLeft = numberOfRetries;
        this.progressListener = progressListener;
        this.cancelledListener = cancelledListener;
        this.fromPercent = fromPercent;
        this.toPercent = toPercent;
    }

    public T call() throws InterruptedException, FailurePermanentException {
        while (true) {
            try {
                return task();
            }
            catch (FailureException failure) {
                if (numberOfTriesLeft == 0) {
                    throw failure;
                }
                numberOfTriesLeft--;
                
                for (int i = 60; i > 0; i--) { // display second countdown
                    ifCancelledThrow();
                    progressReport(lastPercent, failure, i);
                    Thread.sleep(1000);
                }
            }
        }
    }
    
    abstract protected T task() throws FailurePermanentException, InterruptedException;
    
    protected void ifCancelledThrow() throws InterruptedException {
        cancelledListener.ifCancelledThrow();
    }
    
    protected void progressReport(int percent, String message) {
        progressReport(percent, message, null);
    }
    protected void progressReport(int percent, String message, String detail) {
        this.lastPercent = percent;
        
        progressListener.progressReport(ProgressInfo.create(scalePercent(percent), message, detail));
    }
    protected void progressReport(int percent, FailurePermanentException failure, int retryIn) {
        this.lastPercent = percent;
        progressListener.progressReport(ProgressInfo.create(scalePercent(percent), failure, retryIn));
    }
    
    private int scalePercent(int percent) {
        int range = toPercent - fromPercent;
        
        percent = percent / (100/range);
        percent += fromPercent;
        
        return percent;
    }
}
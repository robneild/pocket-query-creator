package org.pquery.webdriver;

import android.os.Bundle;

public class ProgressInfo extends ResultInfo {
	
    public int percent;
    public String htmlMessage;
    
    
    public static ProgressInfo create(int percent, String plainMessage, String goodDetail) {
        if (goodDetail!=null && goodDetail.length()>0)
            return new ProgressInfo(percent, plainMessage + " [" + goodDetail + "]");
        else
            return new ProgressInfo(percent, plainMessage);
    }
    public static ProgressInfo create(int percent, FailurePermanentException failure, int retryIn) {
        return new ProgressInfo(percent, failure.toHTML() + "<br><br>Will try again in " + retryIn + " seconds");
    }
    
    
    public ProgressInfo(int percent, String htmlMessage) {
    	this.percent = percent;
        this.htmlMessage =htmlMessage;
    }
    
    @Override
    public String toString() {
    	return "[percent=" + percent + ",htmlMessage="+htmlMessage+"]";
    }
    
    public ProgressInfo(Bundle bundle) {
    	percent = bundle.getInt("percent");
    	htmlMessage = bundle.getString("htmlMessage");
    }
    
	public void saveToBundle(Bundle bundle) {
		bundle.putInt("percent", percent);
		bundle.putString("htmlMessage", htmlMessage);
		bundle.putInt("msgType", UPDATE);
	}
}

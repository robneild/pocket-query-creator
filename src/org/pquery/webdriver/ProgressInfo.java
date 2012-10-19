package org.pquery.webdriver;

import android.os.Bundle;

public class ProgressInfo extends ResultInfo {
	
    public int percent;
    public String message;
    
    public ProgressInfo(int percent, String message) {
    	this.percent = percent;
        this.message = message;
    }
    
    @Override
    public String toString() {
    	return "[percent=" + percent + ",message="+message+"]";
    }
    
    public ProgressInfo(Bundle bundle) {
    	percent = bundle.getInt("percent");
    	message = bundle.getString("message");
    }
    
	public void saveToBundle(Bundle bundle) {
		bundle.putInt("percent", percent);
		bundle.putString("message", message);
		bundle.putInt("msgType", UPDATE);
	}
}

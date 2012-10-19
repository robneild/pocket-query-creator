package org.pquery.webdriver;

public class Failure {
	public String error;
	public String details;
	
	public Failure(String error) {
		this.error = error;
	}
	
	public Failure(String error, String details) {
		this.error = error;
		this.details = details;
	}
	
	public Failure(String error, Exception details) {
		this.error = error;
		this.details = details.getMessage();
	}
	
	@Override
	public String toString() {
		return "[error=" + error + ",details=" + details + "]";
	}
	
	public String toHTML() {
	    String ret = error;
	    if (details!=null) 
	        ret +=  "<font color=red><small><br>[" + details + "]</small></font>";
		return ret;
	}
}

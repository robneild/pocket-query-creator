package org.pquery.webdriver;

public class FailureException extends FailurePermanentException {

	public FailureException(String error) {
	    super(error);
	}
	
	public FailureException(String error, String details) {
	    super(error);
	}
	
	public FailureException(String error, Exception details) {
	    super(error, details);
	}

	
}

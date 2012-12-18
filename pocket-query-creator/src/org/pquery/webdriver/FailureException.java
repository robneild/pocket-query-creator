package org.pquery.webdriver;

public class FailureException extends FailurePermanentException {

	private static final long serialVersionUID = 5137035404012015228L;

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

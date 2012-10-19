package org.pquery.webdriver;

public class FailureDontRetry extends Exception {

	public FailureDontRetry(String message) {
		super(message);
	}
}

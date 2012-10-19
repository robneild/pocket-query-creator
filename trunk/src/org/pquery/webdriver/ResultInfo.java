package org.pquery.webdriver;

import junit.framework.Assert;
import android.os.Bundle;

public abstract class ResultInfo {

	public static final int FAILURE = 1;
	public static final int SUCCESS = 2;
	public static final int UPDATE = 3;

	
	public static ResultInfo createFromBundle(Bundle bundle) {
		switch (bundle.getInt("msgType")) {
		case FAILURE:
			return new FailureResult(bundle);
		case SUCCESS:
			return new SuccessResult(bundle);
		case UPDATE:
			return new ProgressInfo(bundle);
		}
		Assert.fail();
		return null;
	}
	
	public abstract void saveToBundle(Bundle bundle);
	

}

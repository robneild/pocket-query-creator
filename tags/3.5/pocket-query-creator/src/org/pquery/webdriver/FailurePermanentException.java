package org.pquery.webdriver;

public class FailurePermanentException extends Exception {

	private static final long serialVersionUID = 7687740322490670417L;
	
	public String error;
    public String details;
    
    public FailurePermanentException(String error) {
        super(error);
        this.error = error;
    }
    
    public FailurePermanentException(String error, String details) {
        super(error);
        this.error = error;
        this.details = details;
    }
    
    public FailurePermanentException(String error, Exception details) {
        super(error, details);
        this.error = error;
        this.details = details.getMessage();
    }
    
    @Override
    public String toString() {
        String ret = error;
        if (details!=null) 
            ret +=  " [" + details + "]";
        return ret;
    }
    
    public String toHTML() {
        String ret = error;
        if (details!=null) 
            ret +=  "<font color=red><small><br>[" + details + "]</small></font>";
        return ret;
    }

}

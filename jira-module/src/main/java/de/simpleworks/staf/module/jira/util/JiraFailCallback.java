package de.simpleworks.staf.module.jira.util;

import com.google.common.base.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.util.concurrent.Effect;

public class JiraFailCallback implements Effect<Throwable> {

	private final static Logger logger = LogManager.getLogger(JiraFailCallback.class);
	
	private final static JiraProperties instance = JiraProperties.getInstance();
	
	@Override
	public void apply(Throwable th) {

		
		if(!(th instanceof RestClientException)) {
			final String msg = "unhandled exception happened, won't catch.";
			logger.error(msg, th);
				
			throw new RuntimeException(msg);
		}
		
		
		RestClientException restException = (RestClientException) th;
		
		Optional<Integer> opStatus =  restException.getStatusCode();
		
		if(!opStatus.isPresent()) {
			final String msg = "unknown error response has been catched.";
			logger.error(msg, th);
				
			throw new RuntimeException(msg);
		}
		
		int statusCode = opStatus.get().intValue();
		
		if(statusCode == 429) {
			logger.error("jira rate limiting might happened", th);
	        
	        try {
	        	
	        	final int WAIT_IN_SECONDS = instance.getRateLimit() * 1000;
	        	
	        	if(logger.isDebugEnabled()) {
	        		logger.debug(String.format("wait '%s' milliseconds for the next attempt to access jira.", Integer.toString(WAIT_IN_SECONDS)), th);
	        	}
	        	
	            Thread.sleep(WAIT_IN_SECONDS);
	        } catch (Exception ex) {
	        	logger.error(ex);
	        }
		}
		else {
			final String msg = String.format("catched error with status code '%s'.", Integer.toString(statusCode));
			logger.error(msg, th);
				
			throw new RuntimeException(msg);
		}    
    }

}

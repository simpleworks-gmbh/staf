package de.simpleworks.staf.module.jira.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.atlassian.util.concurrent.Effect;

public class JiraRateLimitingEffect implements Effect<Throwable> {

	private final static Logger logger = LogManager.getLogger(JiraRateLimitingEffect.class);
	
	private final static JiraProperties instance = JiraProperties.getInstance();
	
	@Override
	public void apply(Throwable th) {

        logger.error("jira rate limiting might happened", th);
        
        try {
        	
        	final int WAIT_IN_SECONDS = instance.getRateLimit() * 1000;
        	
        	if(logger.isTraceEnabled()) {
        		logger.trace("wait '%s' milliseconds for the next attempt to aceces jira.", Integer.toString(WAIT_IN_SECONDS), th);
        	}
        	
            Thread.sleep(WAIT_IN_SECONDS);
        } catch (Exception ex) {
        	logger.error(ex);
        }
    }

}

package com.atlassian.jira.rest.client.internal.async;

import com.atlassian.httpclient.api.Response;

/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications copyright (C) 2022 Simpleworks GmbH
 */

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;

import de.simpleworks.staf.module.jira.exceptions.JiraClientException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import com.google.common.base.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class delegates all calls to given delegate Promise. Additionally it throws new RestClientException
 * with original RestClientException given as a cause, which gives a more useful stack trace.
 */
public class DelegatingPromise<T> implements Promise<T> {

	private static final Logger logger = LogManager.getLogger(DelegatingPromise.class);
	

	// Error code that indicates, to many requests in short time frame
	// https://confluence.atlassian.com/adminjiraserver/adjusting-your-code-for-rate-limiting-987143384.html
	private static final int RATE_LIMIT_EXCEEDED_STATUS_CODE = 429;
	private static final String RETRY_HEADER = "retry-after";
	private static final String X_RATE_LIMIT_INTERVAL_SECONDS_HEADER = "x-ratelimit-interval-seconds";
	private static final String X_RATE_LIMIT_FILLRATE_HEADER =  "x-ratelimit-fillrate";
	
    private final Promise<T> delegate;

    public DelegatingPromise(Promise<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T claim() {
    	
    	T result = null;
    	
        try {
        	result = delegate.claim();
        } catch (RestClientException e) {
        	
        	if(!(e instanceof JiraClientException)) {
        		throw new RestClientException(e);	
        	}
        	
        	DelegatingPromise.logger.error("jira rate limiting might have happened.", e);
        	
        	final JiraClientException jiraClientException = (JiraClientException) e;
        	
        	final Optional<Integer> opStatus = jiraClientException.getStatusCode();
        	
        	if(!opStatus.isPresent()) {
            	DelegatingPromise.logger.error("no status code .");
        		throw new RestClientException(e);	
        	}
        	
        	if(RATE_LIMIT_EXCEEDED_STATUS_CODE != opStatus.get()) {
        		DelegatingPromise.logger.error(String.format("status code '%s' does not match the 'rate limiting one' '%s'.",
        				Integer.toString(opStatus.get())
        				,Integer.toString(RATE_LIMIT_EXCEEDED_STATUS_CODE)));
        		throw new RestClientException(e);	
        	}
        	
        	final Response response =  jiraClientException.getResponse();
        	
        	if(!retryAttempt(response.getHeaders())) {
        		throw new RestClientException(e);	
        	}
        }
    	
    	if(result == null) {

            try {
            	result = delegate.claim();
            } catch (RestClientException e) {
                throw new RestClientException(e);
            }
    	}
    	
    	return result; 
    }
    
    private static boolean retryAttempt(final Map<String, String> headers) {
    	
		int retryAfter = 0;  
	
		try {
			retryAfter = Integer.parseInt(headers.getOrDefault(RETRY_HEADER, "0"));
		}
		catch(Exception ex) {
			//...
		}

		int xRateLimitIntervalSecondsHeader = 0;
		
		try {
			xRateLimitIntervalSecondsHeader = Integer.parseInt(headers.getOrDefault(X_RATE_LIMIT_INTERVAL_SECONDS_HEADER, "0"));
		}
		catch(Exception ex) {
			//...
		}
		
		int xRateLimitFillrateHeader = 0;
		
		try {
			xRateLimitFillrateHeader = Integer.parseInt(headers.getOrDefault(X_RATE_LIMIT_FILLRATE_HEADER, "0"));
		}
		catch(Exception ex) {
			//...
		}
		
		int retryTime = Math.max(xRateLimitFillrateHeader, Math.max(retryAfter, xRateLimitIntervalSecondsHeader)) * 1002;
		
    	boolean result = false; 
    
    	if(DelegatingPromise.logger.isTraceEnabled()) {
    		DelegatingPromise.logger.trace(String.format("wait '%s' seconds for the next attempt to access jira.", Long.toString(retryTime)));
    	}
    
    	try {
			Thread.sleep(retryTime);
			result = true;
		} catch (InterruptedException e) {
			DelegatingPromise.logger.trace(String.format("can't wait '%s' seconds for the next attempt to access jira.", Long.toString(retryTime)), e);
		
		}
    
    	return result;
    }

    @Override
    public Promise<T> done(Effect<? super T> e) {
        return delegate.done(e);
    }

    @Override
    public Promise<T> fail(Effect<Throwable> e) {
        return delegate.fail(e);
    }

    @Override
    public Promise<T> then(FutureCallback<? super T> callback) {
        return delegate.then(callback);
    }

    @Override
    public <B> Promise<B> map(Function<? super T, ? extends B> function) {
        return delegate.map(function);
    }

    @Override
    public <B> Promise<B> flatMap(Function<? super T, ? extends Promise<? extends B>> function) {
        return delegate.flatMap(function);
    }

    @Override
    public Promise<T> recover(Function<Throwable, ? extends T> handleThrowable) {
        return delegate.recover(handleThrowable);
    }

    @Override
    public <B> Promise<B> fold(Function<Throwable, ? extends B> handleThrowable, Function<? super T, ? extends B> function) {
        return delegate.fold(handleThrowable, function);
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        delegate.addListener(listener, executor);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return delegate.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return delegate.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.get(timeout, unit);
    }
}

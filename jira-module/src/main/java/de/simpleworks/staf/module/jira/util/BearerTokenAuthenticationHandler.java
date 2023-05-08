package de.simpleworks.staf.module.jira.util;

import com.atlassian.httpclient.api.Request.Builder;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;

import de.simpleworks.staf.commons.utils.Convert;

public class BearerTokenAuthenticationHandler implements AuthenticationHandler {

	private String token; 
	
	public BearerTokenAuthenticationHandler(String token){
		
		if(Convert.isEmpty(token)) {
			throw new IllegalArgumentException("token can't be null or empty string.");
		}
		
		this.token = token;
		
	}
	
	@Override
	public void configure(Builder builder) {
		builder.setHeader("Authorization", String.format("Bearer %s", token));
	}

}

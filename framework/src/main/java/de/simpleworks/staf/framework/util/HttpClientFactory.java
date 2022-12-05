package de.simpleworks.staf.framework.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.framework.api.httpclient.HttpClient;

public class HttpClientFactory {
	
	private static final String HTTP_CLIENT_CLASS = "http.client.class";
	private static final Logger logger = LogManager.getLogger(HttpClientFactory.class);
	
	@SuppressWarnings("deprecation")
	public static HttpClient createHttpClient() throws RuntimeException{

		Class<?> clazz =  null;

		try {
			clazz =  Class.forName(System.getProperty(HTTP_CLIENT_CLASS, HttpClient.class.toString()));	
		}
		catch(Exception ex) {
			final String msg = String.format("can't access class '%s'.", System.getProperty(HTTP_CLIENT_CLASS, HttpClient.class.toString()));
			logger.error(msg,ex);
			throw new RuntimeException(msg);
		}
		
			
		@SuppressWarnings("deprecation")
		Object instance = null;
		
		try {
			instance =  clazz.newInstance();
		}
		catch(Exception ex) {
			final String msg = String.format("can't initialize instance of class '%s'.", System.getProperty(HTTP_CLIENT_CLASS, HttpClient.class.toString()));
			logger.error(msg,ex);
			throw new RuntimeException(msg);
		}
		
		if(!(instance instanceof HttpClient)){
            throw new RuntimeException(String.format("can't create instance of HttpClient from '%s'.", instance));
        }

        return (HttpClient) instance;
    }
}

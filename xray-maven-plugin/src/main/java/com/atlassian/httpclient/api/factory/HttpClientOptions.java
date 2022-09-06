package com.atlassian.httpclient.api.factory;

import com.atlassian.httpclient.api.Request;
import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Effects;
import com.atlassian.util.concurrent.ThreadFactories;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Configuration options for the http client instance and its caching system
 */

/*
 * Modifications copyright (C) 2021 Simpleworks GmbH
 */

public final class HttpClientOptions 
{
    public static final String OPTION_PROPERTY_PREFIX = "com.atlassian.httpclient.options";
    public static final String OPTION_THREAD_WORK_QUEUE_LIMIT = OPTION_PROPERTY_PREFIX + ".threadWorkQueueLimit";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String threadPrefix = "httpclient";
    private boolean ignoreCookies = false;
    private int ioThreadCount = Integer.getInteger(OPTION_PROPERTY_PREFIX + ".ioThreadCount", 10);
    private long ioSelectInterval = Integer.getInteger(OPTION_PROPERTY_PREFIX + ".ioSelectInterval", 1000);
    private int threadWorkQueueLimit = Integer.getInteger(OPTION_THREAD_WORK_QUEUE_LIMIT, 256);

    private long connectionTimeout = Long.getLong(OPTION_PROPERTY_PREFIX + ".connectionTimeout", 5) * 1000;
     
    private long socketTimeout = Long.getLong(OPTION_PROPERTY_PREFIX + ".socketTimeout", 20) * 1000;
    
    private long requestTimeout = Long.getLong(OPTION_PROPERTY_PREFIX + ".requestTimeout", 30) * 1000;
    
    private int maxTotalConnections = 20;
    private int maxConnectionsPerHost = 20;

    private long connectionPoolTimeToLive = 30 * 1000;

    private long maxCacheObjectSize = 100 * 1024L;
    private int maxCacheEntries = 100;

    private long maxEntitySize = 1024 * 1024 * 100;

    private long leaseTimeout = 10 * 60 * 1000; // 10 mins
    private int maxCallbackThreadPoolSize = 16;

    private boolean trustSelfSignedCertificates = false;

    private Effect<Request> requestPreparer = Effects.noop();

    private String userAgent = "Default";

    private ExecutorService callbackExecutor;

    private ProxyOptions proxyOptions = ProxyOptions.ProxyOptionsBuilder.create().build();

    /**
     * Whether or not to ignore cookies.
     * <p/>
     * Default: <code>false</code>
     */
    public boolean getIgnoreCookies()
    {
        return ignoreCookies;
    }

    /**
     * @param ignoreCookies Whether or not to ignore cookies.
     */
    public void setIgnoreCookies(boolean ignoreCookies)
    {
        this.ignoreCookies = ignoreCookies;
    }

    /**
     * Determines the number of I/O dispatch threads to be used by the I/O reactor.
     * <p/>
     * Default: <code>10</code>
     */
    public int getIoThreadCount()
    {
        return ioThreadCount;
    }

    /**
     * @param ioThreadCount The number of I/O dispatch threads to be used by the I/O reactor.
     * May not be negative or zero.
     */
    public void setIoThreadCount(int ioThreadCount)
    {
        this.ioThreadCount = ioThreadCount;
    }

    /**
     * Determines time interval in milliseconds at which the I/O reactor wakes up to check for
     * timed out sessions and session requests.
     * <p/>
     * Default: <code>1000</code> milliseconds.
     */
    public long getIoSelectInterval()
    {
        return ioSelectInterval;
    }

    /**
     * Defines time interval in milliseconds at which the I/O reactor wakes up to check for
     * timed out sessions and session requests. May not be negative or zero.
     */
    public void setIoSelectInterval(int ioSelectInterval, TimeUnit timeUnit)
    {
        this.ioSelectInterval = timeUnit.toMillis(ioSelectInterval);
    }

    /**
     * @return How long, in milliseconds, to wait for a TCP connection
     */
    public long getConnectionTimeout()
    {
        return connectionTimeout;
    }

    /**
     * Sets how long, in milliseconds, to wait for a TCP connection
     *
     * @param connectionTimeout Timeout value, defaults to 5000 milliseconds
     * @param timeUnit The time unit
     */
    public void setConnectionTimeout(int connectionTimeout, TimeUnit timeUnit)
    {
        this.connectionTimeout = timeUnit.toMillis(connectionTimeout);
    }

    /**
     * @return How long, in milliseconds, to wait for data over the socket
     */
    public long getSocketTimeout()
    {
        return socketTimeout;
    }

    /**
     * @param socketTimeout How long to wait for data, defaults to 20 seconds
     * @param timeUnit The time unit
     */
    public void setSocketTimeout(int socketTimeout, TimeUnit timeUnit)
    {
        this.socketTimeout = timeUnit.toMillis(socketTimeout);
    }

    /**
     * @return How long to wait for the entire request
     */
    public long getRequestTimeout()
    {
        return requestTimeout;
    }

    /**
     * @param requestTimeout How long to wait for the entire request.  Defaults to 30 seconds.
     * @param timeUnit The time unit
     */
    public void setRequestTimeout(int requestTimeout, TimeUnit timeUnit)
    {
        this.requestTimeout = timeUnit.toMillis(requestTimeout);
    }

    /**
     * @return The user agent string
     */
    public String getUserAgent()
    {
        return userAgent;
    }

    /**
     * @param userAgent The user agent string
     */
    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    /**
     * @return Name prefix to use for spawned threads
     */
    public String getThreadPrefix()
    {
        return threadPrefix;
    }

    /**
     * @param threadPrefix Name prefix to use for spawned threads
     */
    public void setThreadPrefix(String threadPrefix)
    {
        this.threadPrefix = threadPrefix;
    }

    /**
     * @return How long, in milliseconds, to allow connections to live in the pool.  Defaults
     * to 30 seconds.
     */
    public long getConnectionPoolTimeToLive()
    {
        return connectionPoolTimeToLive;
    }

    /**
     * @param connectionPoolTimeToLive How long to allow connections to live in the pool
     * @param timeUnit The time unit
     */
    public void setConnectionPoolTimeToLive(int connectionPoolTimeToLive, TimeUnit timeUnit)
    {
        this.connectionPoolTimeToLive = timeUnit.toMillis(connectionPoolTimeToLive);
    }

    /**
     * @return How many simultaneous connections are allowed in total. Defaults to 20
     */
    public int getMaxTotalConnections()
    {
        return maxTotalConnections;
    }

    /**
     * @param maxTotalConnections How many simultaneous connections are allowed in total
     */
    public void setMaxTotalConnections(int maxTotalConnections)
    {
        this.maxTotalConnections = maxTotalConnections;
    }

    /**
     * @return How many simultaneous connections are allowed per host. Defaults to 20
     */
    public int getMaxConnectionsPerHost()
    {
        return maxConnectionsPerHost;
    }

    /**
     * @param maxConnectionsPerHost How many connections are allowed per host
     */
    public void setMaxConnectionsPerHost(int maxConnectionsPerHost)
    {
        this.maxConnectionsPerHost = maxConnectionsPerHost;
    }

    /**
     * @return The max object size, in bytes, allowed in the HTTP cache.  Defaults to 100k
     */
    public long getMaxCacheObjectSize()
    {
        return maxCacheObjectSize;
    }

    /**
     * @param maxCacheObjectSize The max cache object size in bytes
     */
    public void setMaxCacheObjectSize(long maxCacheObjectSize)
    {
        this.maxCacheObjectSize = maxCacheObjectSize;
    }

    /**
     * @return The max cache entries.  Defaults to 1000.
     */
    public int getMaxCacheEntries()
    {
        return maxCacheEntries;
    }

    /**
     * @param maxCacheEntries The max cache entries
     */
    public void setMaxCacheEntries(int maxCacheEntries)
    {
        this.maxCacheEntries = maxCacheEntries;
    }

    /**
     * @return The effect to apply before the request is executed
     */
    public Effect<Request> getRequestPreparer()
    {
        return requestPreparer;
    }

    /**
     * @param requestPreparer The effect to apply before the request is executed
     */
    public void setRequestPreparer(Effect<Request> requestPreparer)
    {
        this.requestPreparer = requestPreparer;
    }

    /**
     * @return The maximum entity size in bytes.  Default is 100MB
     */
    public long getMaxEntitySize()
    {
        return maxEntitySize;
    }

    /**
     * @return The maximum time request to be kept in queue before execution, after timeout - request will be removed
     */
    public long getLeaseTimeout()
    {
        return leaseTimeout;
    }

    /**
     * @param leaseTimeout The maximum time request to be kept in queue before execution, after timeout - request will be removed
     */
    public void setLeaseTimeout(long leaseTimeout)
    {
        this.leaseTimeout = leaseTimeout;
    }

    /**
     * param maxEntitySize The maximum entity size in bytes
     */
    public void setMaxEntitySize(long maxEntitySize)
    {
        this.maxEntitySize = maxEntitySize;
    }

    /**
     * @return The maximum number of threads that can be used for executing callbacks
     */
    public int getMaxCallbackThreadPoolSize()
    {
        return maxCallbackThreadPoolSize;
    }

    /**
     * @param maxCallbackThreadPoolSize The maximum number of threads that can be used for executing callbacks
     */
    public void setMaxCallbackThreadPoolSize(final int maxCallbackThreadPoolSize)
    {
        this.maxCallbackThreadPoolSize = maxCallbackThreadPoolSize;
    }

    public void setCallbackExecutor(ExecutorService callbackExecutor)
    {
        this.callbackExecutor = callbackExecutor;
    }

    public ExecutorService getCallbackExecutor()
    {
        return callbackExecutor != null ? callbackExecutor : defaultCallbackExecutor();
    }

    private ExecutorService defaultCallbackExecutor()
    {
        ThreadFactory threadFactory = ThreadFactories.namedThreadFactory(getThreadPrefix() + "-callbacks", ThreadFactories.Type.DAEMON);
        return new ThreadPoolExecutor(
                0,
                getMaxCallbackThreadPoolSize(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(threadWorkQueueLimit),
                threadFactory,
                (r, e) -> log.warn(
                        "Exceeded the limit of requests waiting for execution. " +
                                " Increase the value of the system property {} to prevent these situations in the " +
                                "future. Current value of {} = {}.",
                        OPTION_THREAD_WORK_QUEUE_LIMIT,
                        OPTION_THREAD_WORK_QUEUE_LIMIT,
                        threadWorkQueueLimit)
        );
    }

    public void setTrustSelfSignedCertificates(boolean trustSelfSignedCertificates)
    {
        this.trustSelfSignedCertificates = trustSelfSignedCertificates;
    }

    /**
     * @return whether self signed certificates are trusted.
     */
    public boolean trustSelfSignedCertificates()
    {
        return trustSelfSignedCertificates;
    }

    /**
     * Set proxy options for the client
     * @param proxyOptions Proxy options created using {@link com.atlassian.httpclient.api.factory.ProxyOptions.ProxyOptionsBuilder}.
     */
    public void setProxyOptions(final @Nonnull ProxyOptions proxyOptions)
    {
        Preconditions.checkNotNull(proxyOptions, "Proxy options cannot be null");
        this.proxyOptions = proxyOptions;
    }

    /**
     * @return The proxy options to use for the client.
     */
    public ProxyOptions getProxyOptions()
    {
        return this.proxyOptions;
    }

    public int getThreadWorkQueueLimit() {
        return threadWorkQueueLimit;
    }

    public void setThreadWorkQueueLimit(int threadWorkQueueLimit) {
        this.threadWorkQueueLimit = threadWorkQueueLimit;
    }
}

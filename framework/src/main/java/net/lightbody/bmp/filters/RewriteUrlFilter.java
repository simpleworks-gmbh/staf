package net.lightbody.bmp.filters;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.proxy.RewriteRule;
import net.lightbody.bmp.util.BrowserMobHttpUtil;
import net.lightbody.bmp.util.HttpUtil;

/**
 * Applies rewrite rules to the specified request. If a rewrite rule matches,
 * the request's URI will be overwritten with the rewritten URI. The filter does
 * not make a defensive copy of the rewrite rule collection, so there is no
 * guarantee that the collection at the time of construction will contain the
 * same values when the filter is actually invoked, if the collection is
 * modified concurrently.
 */

/*
 * Modifications copyright (C) 2021 Simpleworks GmbH
 */

/*
 * Original at https://github.com/lightbody/browsermob-proxy
 */

public class RewriteUrlFilter extends HttpsAwareFiltersAdapter {
	private static final Logger log = LoggerFactory.getLogger(RewriteUrlFilter.class);

	private final Collection<RewriteRule> rewriteRules;

	public RewriteUrlFilter(final HttpRequest originalRequest, final ChannelHandlerContext ctx,
			final Collection<RewriteRule> rewriterules) {
		super(originalRequest, ctx);

		if (rewriterules != null) {
			this.rewriteRules = rewriterules;
		} else {
			this.rewriteRules = Collections.emptyList();
		}
	}

	@Override
	public HttpResponse clientToProxyRequest(final HttpObject httpObject) {
		if (httpObject instanceof HttpRequest) {
			final HttpRequest httpRequest = (HttpRequest) httpObject;

			// REWRITE CONNECT
//          if (ProxyUtils.isCONNECT(httpRequest)) {
//              return null;
//          }

			final String originalUrl = getFullUrl(httpRequest);
			String rewrittenUrl = originalUrl;

			boolean rewroteUri = false;
			for (final RewriteRule rule : rewriteRules) {
				final Matcher matcher = rule.getPattern().matcher(rewrittenUrl);
				if (matcher.matches()) {

					rewrittenUrl = matcher.replaceAll(rule.getReplace());
					rewroteUri = true;
				}
			}

			if (rewroteUri) {
				// if the URI in the request contains the scheme, host, and port, the request's
				// URI can be replaced
				// with the rewritten URI. if not (for example, on HTTPS requests), strip the
				// scheme, host, and port from
				// the rewritten URL before replacing the URI on the request.
				final String uriFromRequest = httpRequest.getUri();
				if (HttpUtil.startsWithHttpOrHttps(uriFromRequest)) {

					httpRequest.setUri(rewrittenUrl);
				} else {
					try {
						final String resource = BrowserMobHttpUtil.getRawPathAndParamsFromUri(rewrittenUrl);
						httpRequest.setUri(resource);
					} catch (final URISyntaxException e) {
						// the rewritten URL couldn't be parsed, possibly due to the rewrite rule
						// mangling the URL. log
						// a warning message and replace the resource on the request with the full,
						// rewritten URL.
						RewriteUrlFilter.log.warn(
								"Unable to determine path from rewritten URL. Request URL will be set to the full rewritten URL instead of the resource's path.\n\tOriginal URL: {}\n\tRewritten URL: {}",
								originalUrl, rewrittenUrl, e);

						httpRequest.setUri(rewrittenUrl);
					}
				}

				// determine if the hostname and/or port has been changed by the rewrite rule.
				// if so, update the Host
				// header for HTTP requests. for HTTPS requests, log a warning, since hostname
				// and port cannot be changed
				// by rewrite rules.

				String originalHostAndPort = null;
				try {
					originalHostAndPort = HttpUtil.getHostAndPortFromUri(originalUrl);
				} catch (final URISyntaxException e) {
					// for some reason we couldn't determine the original host and port from the
					// original URL. log a warning,
					// and allow the Host header to be forcibly updated to the rewritten host and
					// port.
					RewriteUrlFilter.log.warn(
							"Unable to determine host and port from original URL. Host header will be set to rewritten URL's host and port.\n\tOriginal URL: {}\n\tRewritten URL: {}",
							originalUrl, rewrittenUrl, e);
				}

				String modifiedHostAndPort = null;
				try {
					modifiedHostAndPort = HttpUtil.getHostAndPortFromUri(rewrittenUrl);
				} catch (final URISyntaxException e) {
					RewriteUrlFilter.log.warn(
							"Unable to determine host and port from rewritten URL. Host header will not be updated.\n\tOriginal URL: {}\n\tRewritten URL: {}",
							originalUrl, rewrittenUrl, e);
				}

				// if the modifiedHostAndPort was parsed successfully and is different from the
				// originalHostAndPort, update the Host header
				if ((modifiedHostAndPort != null) && !modifiedHostAndPort.equals(originalHostAndPort)) {

					if (httpRequest.headers().contains(HttpHeaders.Names.HOST)) {
						HttpHeaders.setHost(httpRequest, modifiedHostAndPort);
					}
				}
			}
		}

		return null;
	}
}

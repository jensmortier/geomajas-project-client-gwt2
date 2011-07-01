/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2011 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */
package org.geomajas.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.geomajas.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Filter} that alters response in two ways: adding cache control headers and compressing the response.
 * Default settings are tuned for delivering GWT stuff in Geomajas context.
 * <p/>
 * When the request is for localhost or 127.0.0.1, then caching headers are not affected, only the gzip compression may
 * be enabled.
 * <p/>
 * This can be controlled using the following context parameters (note that all uris start with a slash and are tested
 * without uri parameters):
 * <ul>
 * <li>cacheDurationInSeconds : time that cache stuff should be cached, defaults to 1 year.</li>
 * <li>skipPrefixes : all uris which start with one of these prefixes remain untouched. Defaults to "/d/".</li>
 * <li>cacheIdentifiers : when the uri contains one of these, the cache headers are added. Defaults to ".cache.".</li>
 * <li>cacheSuffixes : when the uri ends in one of these, the cache headers are added. Defaults to
 * ".js .png .jpg .jpeg .gif .css .html".</li>
 * <li>noCacheIdentifiers : when the uri contains one of these, the cache headers are removed. Defaults to
 * ".nocache.".</li>
 * <li>noCacheSuffixes : when the uri end in one of these, the cache headers are removed. Defaults to "".</li>
 * <li>zipSuffixes : when the uri ends in one of these, the response is gzip compressed. Defaults to
 * ".js .css .html".</li>
 * </ul>
 *
 * @author Pieter De Graef
 * @author Joachim Van der Auwera
 * @since 1.9.0
 */
@Api
public class CacheFilter implements Filter {

	public static final String CACHE_DURATION_IN_SECONDS = "cacheDurationInSeconds";
	public static final String CACHE_IDENTIFIERS = "cacheIdentifiers";
	public static final String CACHE_SUFFIXES = "cacheSuffixes";
	public static final String NO_CACHE_IDENTIFIERS = "noCacheIdentifiers";
	public static final String NO_CACHE_SUFFIXES = "noCacheSuffixes";
	public static final String ZIP_SUFFIXES = "zipSuffixes";
	public static final String SKIP_PREFIXES = "skipPrefixes";
	public static final String PARAMETER_SPLIT_REGEX = "[\\s,]+";

	private long cacheDurationInSeconds = 60 * 60 * 24 * 365; // One year

	private long cacheDurationInMilliSeconds = cacheDurationInSeconds * 1000;

	private static final String HTTP_LAST_MODIFIED_HEADER = "Last-Modified";

	private static final String HTTP_EXPIRES_HEADER = "Expires";
	private static final String HTTP_EXPIRES_HEADER_NOCACHE_VALUE = "Wed, 11 Jan 1984 05:00:00:GMT";

	private static final String HTTP_CACHE_CONTROL_HEADER = "Cache-Control";
	private static final String HTTP_CACHE_CONTROL_HEADER_NOCACHE_VALUE =
			"max-age=0, no-cache, no-store, must-revalidate";

	private static final String HTTP_CACHE_PRAGMA = "Pragma";
	private static final String HTTP_CACHE_PRAGMA_VALUE = "no-cache";

	private String[] cacheIdentifiers = new String[] {".cache."};
	private String[] cacheSuffixes = new String[] {".js", ".png", ".jpg", ".jpeg", ".gif", ".css", ".html"};
	private String[] noCacheIdentifiers = new String[] {".nocache."};
	private String[] noCacheSuffixes = new String[] {};
	private String[] zipSuffixes = new String[] {".js", ".css", ".html"};
	private String[] skipPrefixes = new String[] {"/d/"};

	// ------------------------------------------------------------------------
	// Filter implementation:
	// ------------------------------------------------------------------------

	public void destroy() {
	}

	public void init(FilterConfig config) throws ServletException {
		ServletContext context = config.getServletContext();
		String param;

		param = context.getInitParameter(CACHE_IDENTIFIERS);
		if (null != param) {
			cacheIdentifiers = param.split(PARAMETER_SPLIT_REGEX);
		}

		param = context.getInitParameter(CACHE_SUFFIXES);
		if (null != param) {
			cacheSuffixes = param.split(PARAMETER_SPLIT_REGEX);
		}

		param = context.getInitParameter(NO_CACHE_IDENTIFIERS);
		if (null != param) {
			noCacheIdentifiers = param.split(PARAMETER_SPLIT_REGEX);
		}

		param = context.getInitParameter(NO_CACHE_SUFFIXES);
		if (null != param) {
			noCacheSuffixes = param.split(PARAMETER_SPLIT_REGEX);
		}

		param = context.getInitParameter(ZIP_SUFFIXES);
		if (null != param) {
			zipSuffixes = param.split(PARAMETER_SPLIT_REGEX);
		}

		param = context.getInitParameter(SKIP_PREFIXES);
		if (null != param) {
			skipPrefixes = param.split(PARAMETER_SPLIT_REGEX);
		}

		param = context.getInitParameter(CACHE_DURATION_IN_SECONDS);
		if (null != param) {
			try {
				cacheDurationInSeconds = Integer.parseInt(param);
				cacheDurationInMilliSeconds = cacheDurationInSeconds * 1000;
			} catch (NumberFormatException nfe) {
				throw new ServletException("Cannot parse " + CACHE_DURATION_IN_SECONDS + " value " + param +
						", should be parable to integer", nfe);
			}

		}
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "AvoidUsingHardCodedIP",
			justification = "double-safe check on localhost")
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
			ServletException {
		boolean chainCalled = false;
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			String requestUri = httpRequest.getRequestURI();

			if (!checkPrefixes(requestUri, skipPrefixes)) {
				String serverName = httpRequest.getServerName();
				boolean isLocalhost = "localhost".equals(serverName) || "127.0.0.1".equals(serverName);

				if (!isLocalhost) {
					if (shouldNotCache(requestUri)) {
						configureNoCaching(httpResponse);
					} else if (shouldCache(requestUri)) {
						configureCaching(httpResponse);
					}
				}

				if (shouldCompress(requestUri)) {
					String encodings = httpRequest.getHeader("Accept-Encoding");
					if (encodings != null && encodings.indexOf("gzip") != -1) {
						GzipServletResponseWrapper responseWrapper = new GzipServletResponseWrapper(httpResponse);
						try {
							filterChain.doFilter(request, responseWrapper);
							chainCalled = true;
						} finally {
							responseWrapper.finish();
						}
					}
				}
			}
		}
		if (!chainCalled) {
			filterChain.doFilter(request, response);
		}
	}

	public boolean shouldCache(String requestUri) {
		String uri = requestUri.toLowerCase();
		return checkContains(uri, cacheIdentifiers) || checkSuffixes(uri, cacheSuffixes);
	}

	public boolean shouldNotCache(String requestUri) {
		String uri = requestUri.toLowerCase();
		return checkContains(uri, noCacheIdentifiers) || checkSuffixes(uri, noCacheSuffixes);
	}

	public boolean shouldCompress(String requestUri) {
		String uri = requestUri.toLowerCase();
		return checkSuffixes(uri, zipSuffixes);
	}

	public boolean checkContains(String uri, String[] patterns) {
		for (String pattern : patterns) {
			if (pattern.length() > 0) {
				if (uri.contains(pattern)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean checkSuffixes(String uri, String[] patterns) {
		for (String pattern : patterns) {
			if (pattern.length() > 0) {
				if (uri.endsWith(pattern)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean checkPrefixes(String uri, String[] patterns) {
		for (String pattern : patterns) {
			if (pattern.length() > 0) {
				if (uri.startsWith(pattern)) {
					return true;
				}
			}
		}
		return false;
	}

	// ------------------------------------------------------------------------
	// Private methods for setting cache/no-cache:
	// ------------------------------------------------------------------------

	/**
	 * Configure the HTTP response to switch off caching.
	 *
	 * @param response response to configure
	 * @since 1.9.0
	 */
	@Api
	public static void configureNoCaching(HttpServletResponse response) {
		// HTTP 1.0 header:
		response.setHeader(HTTP_EXPIRES_HEADER, HTTP_EXPIRES_HEADER_NOCACHE_VALUE);
		response.setHeader(HTTP_CACHE_PRAGMA, HTTP_CACHE_PRAGMA_VALUE);

		// HTTP 1.1 header:
		response.setHeader(HTTP_CACHE_CONTROL_HEADER, HTTP_CACHE_CONTROL_HEADER_NOCACHE_VALUE);
	}

	private void configureCaching(HttpServletResponse response) {
		long now = System.currentTimeMillis();
		response.setDateHeader(HTTP_LAST_MODIFIED_HEADER, now);

		// HTTP 1.0 header
		response.setDateHeader(HTTP_EXPIRES_HEADER, now + cacheDurationInMilliSeconds);

		// HTTP 1.1 header
		response.setHeader(HTTP_CACHE_CONTROL_HEADER, "max-age=" + cacheDurationInSeconds);
	}

	// ------------------------------------------------------------------------
	// Private class GzipServletResponseWrapper
	// ------------------------------------------------------------------------

	/**
	 * Wrapper around a response that uses a GZIP stream.
	 *
	 * @author Pieter De Graef
	 */
	private class GzipServletResponseWrapper extends HttpServletResponseWrapper {

		private final Logger log = LoggerFactory.getLogger(GzipServletResponseWrapper.class);

		private ServletOutputStream stream;

		public GzipServletResponseWrapper(HttpServletResponse response) {
			super(response);
		}

		public void finish() {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					log.error("Could not close stream", e);
				}
			}
		}

		public ServletOutputStream getOutputStream() throws IOException {
			if (stream == null) {
				stream = new GzipResponseStream((HttpServletResponse) getResponse());
			}
			return stream;
		}
	}
}

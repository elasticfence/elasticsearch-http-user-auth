package org.elasticsearch.plugin.elasticfence;

import static org.elasticsearch.rest.RestStatus.SERVICE_UNAVAILABLE;

import java.net.InetSocketAddress;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;


import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.plugin.elasticfence.data.UserDataBridge;
import org.elasticsearch.plugin.elasticfence.logger.EFLogger;
import org.elasticsearch.plugin.elasticfence.parser.RequestParser;
import org.elasticsearch.plugin.elasticfence.tool.RequestAnalyzer;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestFilter;
import org.elasticsearch.rest.RestFilterChain;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

public class AuthRestFilter extends RestFilter {
	Settings settings;
	public AuthRestFilter(Settings settings) {
		this.settings = settings;
	}
	@Override
	public void process(RestRequest request, RestChannel channel, NodeClient client, RestFilterChain filterChain) throws Exception {
		try {
			// IP Check
			String ipaddr = ((InetSocketAddress) request.getRemoteAddress()).getAddress().getHostAddress();
            	// Loggers.getLogger(getClass()).error("Request from IP: " + ipaddr);

			IPAuthenticator ipAuthenticator = new IPAuthenticator();
			if ( ipAuthenticator.isWhitelisted(ipaddr) ) {
             	// Loggers.getLogger(getClass()).error("Request from IP is whitelisted: " + ipaddr);
				filterChain.continueProcessing(request, channel, client);
				return;
			} else if ( ipAuthenticator.isBlacklisted(ipaddr) ) {
				EFLogger.error("Request from IP is blacklisted: " + ipaddr);
				BytesRestResponse resp = new BytesRestResponse(RestStatus.FORBIDDEN, "Forbidden IP");
				channel.sendResponse(resp);
				return;
			}

			// auth check
			RequestAnalyzer requestAnalyzer = new RequestAnalyzer(request);
			String username = requestAnalyzer.getUsername();
			String password = requestAnalyzer.getPassword();
			if (username == null || password == null) {
				BytesRestResponse resp = new BytesRestResponse(RestStatus.UNAUTHORIZED, "Needs Basic Auth");
				resp.addHeader("WWW-Authenticate", "Basic realm=\"Http User Auth Plugin\"");
				channel.sendResponse(resp);
				EFLogger.info( ipaddr + " auth failed: " + request.path());
				return ;
			}
			
			if (!username.equals("root")) {
		        UserDataBridge userDataBridge = new UserDataBridge(client);
		        if (!userDataBridge.isInitialized()) {
			        channel.sendResponse(new BytesRestResponse(SERVICE_UNAVAILABLE, "http user auth initializing..."));
			        return ;
		        }
			}
	        EFLogger.debug("request.path(): " + request.path());
			// auth index
			UserAuthenticator userAuth = new UserAuthenticator(username, password);
			RequestParser parser = new RequestParser(request, settings);
			if (userAuth.isValidUser()) {
				boolean isAccessible;
				isAccessible = userAuth.isAccessibleIndices(parser);
				if (isAccessible) {
					try {
						filterChain.continueProcessing(request, channel, client);
					} catch (IndexNotFoundException infe) {
						EFLogger.info("index not found: " + request.path());
					} catch (Exception ex) {
						EFLogger.error("exception occurred: " + request.path(), ex);
					}
			    	return ;
				} else {
					// forbidden path 
					EFLogger.info("forbidden path: " + request.path());
					BytesRestResponse resp = new BytesRestResponse(RestStatus.FORBIDDEN, "Forbidden path");
			        channel.sendResponse(resp);
				}
			} else {
				// auth failed 
				BytesRestResponse resp = new BytesRestResponse(RestStatus.UNAUTHORIZED, "Needs Basic Auth");
				resp.addHeader("WWW-Authenticate", "Basic realm=\"Http User Auth Plugin\"");
		        channel.sendResponse(resp);
		        EFLogger.info("Invalid User: " + request.path());
			}
		} catch (Exception ex) {
	        channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, ""));
	        EFLogger.error("", ex);
		}
        return ;
	}
}

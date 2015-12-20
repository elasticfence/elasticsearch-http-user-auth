package org.elasticsearch.plugin.http.user.auth;

import static org.elasticsearch.rest.RestStatus.SERVICE_UNAVAILABLE;

import java.util.Set;
import java.net.InetSocketAddress;

import org.elasticsearch.client.Client;
import com.google.common.collect.Sets;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.plugin.http.user.auth.data.UserDataBridge;
import org.elasticsearch.plugin.http.user.auth.tool.RequestAnalyzer;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestFilter;
import org.elasticsearch.rest.RestFilterChain;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

public class AuthRestFilter extends RestFilter {
	Client client;
	public AuthRestFilter(Client client) {
		this.client = client;
	}
	@Override
	public void process(RestRequest request, RestChannel channel, RestFilterChain filterChain) throws Exception {
		try {
			// url check
			String pathStr = request.path();
			Set<String> paths = Sets.newConcurrentHashSet();
			if (pathStr.contains(",")) {
				paths = Sets.newHashSet(pathStr.split(","));
			} else {
				paths.add(pathStr);
			}
			
			// IP Check
			String ipaddr = ((InetSocketAddress) request.getRemoteAddress()).getAddress().getHostAddress();
	            	// Loggers.getLogger(getClass()).error("Request from IP: " + ipaddr);

			IPAuthenticator ipAuthenticator = new IPAuthenticator();
			if ( ipAuthenticator.isWhitelisted(ipaddr) ) {
		             	// Loggers.getLogger(getClass()).error("Request from IP is whitelisted: " + ipaddr);
					filterChain.continueProcessing(request, channel);
					return;
			} else if ( ipAuthenticator.isBlacklisted(ipaddr) ) {
		             	Loggers.getLogger(getClass()).error("Request from IP is blacklisted: " + ipaddr);
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
	            Loggers.getLogger(getClass()).error("auth failed: " + request.path());
				return ;
			}
			
			if (!username.equals("root")) {
		        UserDataBridge userDataBridge = new UserDataBridge(client);
		        if (!userDataBridge.isInitialized()) {
			        channel.sendResponse(new BytesRestResponse(SERVICE_UNAVAILABLE, "http user auth initializing..."));
			        return ;
		        }
			}
	        
			UserAuthenticator userAuth = new UserAuthenticator(username, password);
			if (userAuth.isValidUser()) {
				boolean passAll = true;
				for (String path : paths) {
			    	if (!userAuth.execAuth(path)) {
			    		passAll = false;
			    	}
				}
				if (passAll) {
					filterChain.continueProcessing(request, channel);
			    	return ;
				} else {
					// forbidden path 
					BytesRestResponse resp = new BytesRestResponse(RestStatus.FORBIDDEN, "Forbidden path");
			        channel.sendResponse(resp);
				}
			} else {
				// auth failed 
				BytesRestResponse resp = new BytesRestResponse(RestStatus.UNAUTHORIZED, "Needs Basic Auth");
				resp.addHeader("WWW-Authenticate", "Basic realm=\"Http User Auth Plugin\"");
		        channel.sendResponse(resp);
	            Loggers.getLogger(getClass()).error("Invalid User: " + request.path());
			}
		} catch (Exception ex) {
	        channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, ""));
            Loggers.getLogger(getClass()).error("", ex);
		}
        return ;
	}
}

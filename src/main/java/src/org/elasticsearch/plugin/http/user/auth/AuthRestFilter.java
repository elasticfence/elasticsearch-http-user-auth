package org.elasticsearch.plugin.http.user.auth;

import java.util.Set;

import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.http.user.auth.tool.RequestAnalyzer;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestFilter;
import org.elasticsearch.rest.RestFilterChain;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

public class AuthRestFilter extends RestFilter {
	@SuppressWarnings("unused")
	private Settings settings;
	public AuthRestFilter(Settings settings) {
		this.settings = settings;
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
	    	
			// auth check
			RequestAnalyzer requestAnalyzer = new RequestAnalyzer(request);
			String user = requestAnalyzer.getUsername();
			String pass = requestAnalyzer.getPassword();
			if (user == null || pass == null) {
				BytesRestResponse resp = new BytesRestResponse(RestStatus.UNAUTHORIZED, "Needs Basic Auth");
				resp.addHeader("WWW-Authenticate", "Basic realm=\"Http User Auth Plugin\"");
		        channel.sendResponse(resp);
	            Loggers.getLogger(getClass()).info("auth failed: " + request.path());
				return ;
			}

			UserAuthenticator userAuth = new UserAuthenticator(user, pass);
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
	            Loggers.getLogger(getClass()).info("auth failed: " + request.path());
			}
		} catch (Exception ex) {
	        channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, ""));
            Loggers.getLogger(getClass()).error("", ex);
		}
        return ;
	}
}

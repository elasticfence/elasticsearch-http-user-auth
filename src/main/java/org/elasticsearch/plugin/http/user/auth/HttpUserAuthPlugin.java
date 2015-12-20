package org.elasticsearch.plugin.http.user.auth;

import java.util.Arrays;

import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.plugins.Plugin;

public class HttpUserAuthPlugin extends Plugin {

	private final Settings settings;

    	public HttpUserAuthPlugin(Settings settings){	
        	this.settings = settings;
	        Loggers.getLogger(getClass()).info("http-user-auth plugin is loading...");
    	}

        @Override
	public String description() {
		return "Http User Auth plugin";
	}

        @Override
	public String name() {
		return "Http User Auth plugin";
	}


//    public Collection<Class<? extends Module>> modules() {
    public void onModule(RestModule module) {
    	String isPlaginDisabled = settings.get("http.user.auth.disabled");
        Loggers.getLogger(getClass()).info("http.user.auth.disabled: " + isPlaginDisabled);
        
    	if (isPlaginDisabled != null && isPlaginDisabled.toLowerCase().equals("true")) {
            Loggers.getLogger(getClass()).warn("http-user-auth plugin is disabled");

    	} else {
        	String rootPassword = settings.get("http.user.auth.root.password");
        	if (rootPassword != null && !rootPassword.equals("")) {
        		UserAuthenticator.setRootPassword(rootPassword);
        		UserAuthenticator.loadRootUserDataCacheOnStart();
        	}

        	String[] whitelist = settings.getAsArray("http.user.auth.whitelist", new String[]{"127.0.0.1"});
        	String[] blacklist = settings.getAsArray("http.user.auth.blacklist", new String[]{});

        	if (whitelist != null ) {
        		IPAuthenticator.setWhitelist(whitelist);
	            	Loggers.getLogger(getClass()).warn("http-user-auth plugin IP whitelist enabled " + Arrays.toString(whitelist));
        	}
        	if (blacklist != null ) {
        		IPAuthenticator.setBlacklist(blacklist);
	            	Loggers.getLogger(getClass()).warn("http-user-auth plugin IP blacklist enabled " + Arrays.toString(blacklist));
        	}
    		
            module.addRestAction(AuthRestHandler.class);
            Loggers.getLogger(getClass()).info("http-user-auth plugin is enabled");
    	}
    }
}



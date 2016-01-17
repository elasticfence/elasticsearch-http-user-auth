package org.elasticsearch.plugin.elasticfence;

import java.util.Arrays;

import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.plugins.Plugin;

public class ElasticfencePlugin extends Plugin {

	private final Settings settings;

    	public ElasticfencePlugin(Settings settings){	
        	this.settings = settings;
	        Loggers.getLogger(getClass()).info("elasticfence plugin is loading...");
    	}

        @Override
	public String description() {
		return "Elasticfence plugin";
	}

        @Override
	public String name() {
		return "Elasticfence";
	}


//    public Collection<Class<? extends Module>> modules() {
    public void onModule(RestModule module) {
    	String isPluginDisabled = settings.get("elasticfence.disabled");
        Loggers.getLogger(getClass()).info("elasticfence.disabled: " + isPluginDisabled);
        
    	if (isPluginDisabled != null && isPluginDisabled.toLowerCase().equals("true")) {
            Loggers.getLogger(getClass()).warn("Elasticfence plugin is disabled");
    	} else {
        	String rootPassword = settings.get("elasticfence.root.password");
        	if (rootPassword != null && !rootPassword.equals("")) {
        		UserAuthenticator.setRootPassword(rootPassword);
        		UserAuthenticator.loadRootUserDataCacheOnStart();
        	}

        	String[] whitelist = settings.getAsArray("elasticfence.whitelist", new String[]{"127.0.0.1"});
        	String[] blacklist = settings.getAsArray("elasticfence.blacklist", new String[]{});

        	if (whitelist != null ) {
        		IPAuthenticator.setWhitelist(whitelist);
	            	Loggers.getLogger(getClass()).warn("elasticfence plugin IP whitelist enabled " + Arrays.toString(whitelist));
        	}
        	if (blacklist != null ) {
        		IPAuthenticator.setBlacklist(blacklist);
	            	Loggers.getLogger(getClass()).warn("elasticfence plugin IP blacklist enabled " + Arrays.toString(blacklist));
        	}
    		
            module.addRestAction(AuthRestHandler.class);
            Loggers.getLogger(getClass()).info("elasticfence plugin is enabled");
    	}
    }
}



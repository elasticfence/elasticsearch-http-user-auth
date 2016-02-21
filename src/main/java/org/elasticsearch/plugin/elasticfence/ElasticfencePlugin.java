package org.elasticsearch.plugin.elasticfence;

import java.util.Arrays;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.elasticfence.logger.EFLogger;
import org.elasticsearch.rest.RestModule;

import org.elasticsearch.plugins.Plugin;

public class ElasticfencePlugin extends Plugin {

	private final Settings settings;

    	public ElasticfencePlugin(Settings settings){	
        	this.settings = settings;
        	EFLogger.info("loading elasticfence plugin...");
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
    	String isPluginDisabled = getSettingString("disabled");
        
    	if (isPluginDisabled != null && isPluginDisabled.equals("true")) {
            EFLogger.warn("Elasticfence plugin is disabled");
    	} else {
        	String rootPassword = getSettingString("root.password");
        	if (rootPassword != null && !rootPassword.equals("")) {
        		UserAuthenticator.setRootPassword(rootPassword);
        		UserAuthenticator.loadRootUserDataCacheOnStart();
        	}

        	String[] whitelist = getSettingArray("whitelist", new String[]{"127.0.0.1"});
        	String[] blacklist = getSettingArray("blacklist", new String[]{});

        	if (whitelist != null ) {
        		IPAuthenticator.setWhitelist(whitelist);
	            	EFLogger.warn("elasticfence plugin IP whitelist enabled " + Arrays.toString(whitelist));
        	}
        	if (blacklist != null ) {
        		IPAuthenticator.setBlacklist(blacklist);
	            	EFLogger.warn("elasticfence plugin IP blacklist enabled " + Arrays.toString(blacklist));
        	}
    		
            module.addRestAction(AuthRestHandler.class);
            EFLogger.info("elasticfence plugin is enabled");
    	}
    }

    private String getSettingString(String key) {
    	String flag = settings.get("elasticfence." + key);
    	EFLogger.info("elasticfence." + key + ": " + flag);
    	if (flag == null) {
    		flag = settings.get("http.user.auth." + key);
        	EFLogger.info("http.user.auth." + key + ": " + flag);
        	EFLogger.warn("\"http.user.auth." + key + "\" is deprecated. Please replace with \"elasticfence." + key + "\"");
    	}
    	return flag;
    }
    private String[] getSettingArray(String key, String[] defaultValue) {
    	String[] flag = settings.getAsArray("elasticfence." + key);
    	if (flag == null || flag.length == 0) {
    		flag = settings.getAsArray("http.user.auth." + key);
        	EFLogger.warn("\"http.user.auth." + key + "\" is deprecated. Please replace with \"elasticfence." + key + "\"");
    	}
    	if (flag == null || flag.length == 0) {
    		return defaultValue;
    	}
    	return flag;
    }
}



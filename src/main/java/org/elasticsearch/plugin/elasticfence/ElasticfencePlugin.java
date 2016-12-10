package org.elasticsearch.plugin.elasticfence;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.elasticfence.logger.EFLogger;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.rest.RestHandler;

import org.elasticsearch.plugins.Plugin;

import static java.util.Collections.singletonList;

public class ElasticfencePlugin extends Plugin implements ActionPlugin {

	private final Settings settings;

	public ElasticfencePlugin(Settings settings){
		this.settings = settings;
		EFLogger.info("loading elasticfence plugin...");
	}

	@Override
	public Settings additionalSettings() {
		return Settings.EMPTY;
	}

	@Override
	public List<Class<? extends RestHandler>> getRestHandlers() {
		String isPluginDisabled = getSettingString("disabled");

		if (isPluginDisabled != null && "true".equals(isPluginDisabled)) {
			EFLogger.warn("Elasticfence plugin is disabled");
		} else {
			String rootPassword = "rootPassword"; //getSettingString("root.password");
			if (rootPassword != null && !"".equals(rootPassword)) {
				UserAuthenticator.setRootPassword(rootPassword);
				UserAuthenticator.loadRootUserDataCacheOnStart();
			}

			String[] whitelist = getSettingArray("whitelist", new String[]{"127.0.0.1"});
			String[] blacklist = getSettingArray("blacklist", new String[]{});

			if (whitelist != null) {
				IPAuthenticator.setWhitelist(whitelist);
				EFLogger.warn("elasticfence plugin IP whitelist enabled " + Arrays.toString(whitelist));
			}
			if (blacklist != null) {
				IPAuthenticator.setBlacklist(blacklist);
				EFLogger.warn("elasticfence plugin IP blacklist enabled " + Arrays.toString(blacklist));
			}

			EFLogger.info("elasticfence plugin is enabled");
		}

		return singletonList(AuthRestHandler.class);
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



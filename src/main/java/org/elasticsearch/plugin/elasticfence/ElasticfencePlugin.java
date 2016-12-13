package org.elasticsearch.plugin.elasticfence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.elasticfence.logger.EFLogger;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.rest.RestHandler;

import org.elasticsearch.plugins.Plugin;

import static java.util.Collections.singletonList;

public class ElasticfencePlugin extends Plugin implements ActionPlugin {
	private final Settings settings;
    private final String SETTINGS_PREFIX = "elasticfence.";

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
        try {
            String isPluginDisabled = getSettingString("disabled");

            if (isPluginDisabled != null && "true".equals(isPluginDisabled)) {
                EFLogger.warn("Elasticfence plugin is disabled");
            } else {
                String rootPassword = getSettingString("root.password");
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
                return singletonList(AuthRestHandler.class);
            }
        } catch (Exception e) {
            EFLogger.error("Error occurred during initialization of Elasticfence!", e);
        }

        return Collections.emptyList();
	}

	private String getSettingString(String key) throws Exception {
        Settings s = settings.getByPrefix(SETTINGS_PREFIX);
        String value = s.get(key);

		//EFLogger.info(SETTINGS_PREFIX + key + " value: " + value);

		if (value == null) {
			throw new Exception(key + " is not defined in settings!");
		}

		return value;
	}

	private String[] getSettingArray(String key, String[] defaultValue) throws Exception {
        Settings s = settings.getByPrefix(SETTINGS_PREFIX);
        String[] value = s.getAsArray(key, defaultValue);

        if (value == null || value.length == 0) {
            EFLogger.warn(key + " is not defined in settings, setting default value of " +  Arrays.toString(defaultValue));
            return defaultValue;
        }

        //EFLogger.info(key + ": " + Arrays.toString(value));
        return value;
	}

    private Setting<Boolean> asBoolean(String name) {
        return Setting.boolSetting(name, Boolean.FALSE, Setting.Property.NodeScope);
    }

    private Setting<String> asString(String name) {
        return new Setting<>(name, "", (value) -> value, Setting.Property.NodeScope);
    }

    private Setting<List<String>> asList(String name){
        return Setting.listSetting(name, new ArrayList<>(), s -> s.toString(), Setting.Property.NodeScope);
    }

    @Override
    public List<Setting<?>> getSettings() {
        String rootPrefix = "root.";

        return Arrays.asList(
            asBoolean(SETTINGS_PREFIX + "disabled"),
            asString(SETTINGS_PREFIX + rootPrefix + "password"),
            asList(SETTINGS_PREFIX + "whitelist"),
            asList(SETTINGS_PREFIX + "blacklist")
        );
    }
}



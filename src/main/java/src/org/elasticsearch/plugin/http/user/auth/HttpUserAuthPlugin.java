package org.elasticsearch.plugin.http.user.auth;
import java.util.Collection;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.AbstractPlugin;

public class HttpUserAuthPlugin extends AbstractPlugin {
    private Settings settings;
    @Inject public HttpUserAuthPlugin(Settings settings) {
        this.settings = settings;
    }
	public String description() {
		return "Http User Auth plugin";
	}

	public String name() {
		return "Http User Auth plugin";
	}

    @Override
    public Collection<Class<? extends Module>> modules() {
    	String isPlaginDisabled = settings.get("http.user.auth.disabled");
        Loggers.getLogger(getClass()).info("http.user.auth.disabled: " + isPlaginDisabled);
        
    	if (isPlaginDisabled != null && isPlaginDisabled.toLowerCase().equals("true")) {
            Loggers.getLogger(getClass()).warn("http-user-auth plugin is disabled");
    		return Lists.newArrayList();
    	} else {
        	String rootPassword = settings.get("http.user.auth.root.password");
        	if (rootPassword != null && !rootPassword.equals("")) {
        		UserAuthenticator.setRootPassword(rootPassword);
        		UserAuthenticator.reloadUserDataCache(null);
        	}
    		
            Collection<Class<? extends Module>> modules = Lists.newArrayList();
            modules.add(AuthRestModule.class);
            Loggers.getLogger(getClass()).info("http-user-auth plugin is enabled");
            return modules;
    	}
    }
}



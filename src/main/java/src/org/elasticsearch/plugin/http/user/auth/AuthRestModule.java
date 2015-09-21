package org.elasticsearch.plugin.http.user.auth;

import org.elasticsearch.common.inject.AbstractModule;

public class AuthRestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AuthRestHandler.class).asEagerSingleton();
    }
}
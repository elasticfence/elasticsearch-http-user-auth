package org.elasticsearch.plugin.elasticfence;

/**
 * Class for holding settings
 */
class ElasticfenceSettings {
    static final String SETTINGS_PREFIX = "elasticfence.";

    //default sharding scheme for ES...are these defined as constants in the ES code anywhere??
    static final int SETTING_AUTH_NUMBER_OF_SHARDS_DEFAULT = 5;
    static final int SETTING_AUTH_NUMBER_OF_REPLICAS_DEFAULT = 1;
    static int SETTING_AUTH_NUMBER_OF_SHARDS;
    static int SETTING_AUTH_NUMBER_OF_REPLICAS;
}

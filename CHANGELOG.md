All Elasticfence version numbers correspond to Elasticsearch version numbers (e.g. version 5.0.0 of Elasticfence is for Elasticsearch version 5.0.0).  The following change log is related to features specific to this plugin.  Changes associated with getting everything working with new versions of Elasticsearch will not be documented here.

# 5.1.1

- Add option to specify a wildcard blacklist option to block all IPs that are not in the whitelist.
- Add options to specify how many primary/replica shards the `.http_user_auth` index should have.  If no options specified, it will use the ES defaults.

# 5.0.0

- Removed deprecated `http.auth` related config options.  Please use `elasticfence` prefixed config options.
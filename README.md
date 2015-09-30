# Elasticsearch HTTP Basic User Auth plugin (and its web console)

Elasticsearch user authentication plugin with http basic auth.
This plugin provides user authentication APIs and its web console. 

## Installation 
bin/plugin --url https://raw.githubusercontent.com/TomSearch/elasticsearch-http-user-auth/master/jar/http-user-auth-plugin-1.0-SNAPSHOT.jar --install http-user-auth-plugin;

## Configuration
Add following lines to elasticsearch.yml:
<pre>
http.user.auth.disabled: false
http.user.auth.root.password: yourpassword
</pre>

If you set `http.user.auth.disabled` to `true`, Elasticsearch won't load this plugin at boot time. 

## User Management Console

This plugin also provide 
http://your.elasticsearch.hostname:9200/_plugin/http-user-auth-plugin/index.html

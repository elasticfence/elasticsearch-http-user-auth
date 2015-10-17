# Elasticsearch HTTP Basic User Auth plugin (and its web console)

Elasticsearch user authentication plugin with http basic auth.
This plugin provides user authentication APIs and its web console. 

## Installation 
<pre>
bin/plugin --url https://raw.githubusercontent.com/TomSearch/elasticsearch-http-user-auth/master/jar/http-user-auth-plugin-1.0-SNAPSHOT.jar --install http-user-auth-plugin
</pre>

## Configuration
Add following lines to elasticsearch.yml:
<pre>
http.user.auth.disabled: false
http.user.auth.root.password: rootpassword
</pre>

If you set `http.user.auth.disabled` to `true`, Elasticsearch won't load this plugin.  
`http.user.auth.root.password` sets root user's password literally.  
**Only the root user can access ES's root APIs (like /_cat, /_cluster) and all indices.**  
Other users can access URLs under their own indices that are specified with this plugin's API.  

## Add username and password on HTTP requests
The authentication method of this plugin is Basic Authentication. Therefore, you should add your username and password on URL string. 

For example, you can access the "You know, for search" API from *http://root:rootpassword@your.elasticsearch.hostname:9200/*

Plugins using ES's REST API also have to be set root password in their configurations.

The ways of configuring Marvel and Kibana 4 are below: 

#### Marvel 
elasticsearch.yml:
<pre>
marvel.agent.exporter.es.hosts: ["root:rootpassword@127.0.0.1:9200"]
</pre>

#### Kibana 4
kibana.yml: 
<pre>
elasticsearch_url: "http://root:rootpassword@localhost:9200"
</pre>


## User Management Console

This plugin provides a web console which manages users. 
<pre>
http://your.elasticsearch.hostname:9200/_plugin/http-user-auth-plugin/index.html
</pre>

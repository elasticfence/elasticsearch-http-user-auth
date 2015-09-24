package org.elasticsearch.plugin.http.user.auth.data;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugin.http.user.auth.UserAuthenticator;
import org.elasticsearch.search.SearchHit;

import static org.elasticsearch.common.xcontent.XContentFactory.*;
/**
 * A bridge class of UserData and Elasticsearch index data. 
 * @author tk
 */
public class UserDataBridge {
	private static final String HTTP_USER_AUTH_INDEX = ".http_user_auth";
	private static final String HTTP_USER_AUTH_TYPE = "user";
	private static boolean isInitialized = false;
	private Client client;
	
	public UserDataBridge(Client client) {
		this.client = client;
		if (!createIndexIfEmpty()) {
			Loggers.getLogger(getClass()).error("failed to create index: " + HTTP_USER_AUTH_INDEX);
		}
	}
	
	public boolean isInitialized() {
		if (!isInitialized) {
			reloadUserDataCache();
		}
		// check again after the initialization 
		if (isInitialized) {
			return true;
		}

		return false;
	}
	
	public String listUser () {
		String response = "[";
		List<UserData> allUserData = getAllUserData();
		for (UserData userData : allUserData) {
			response += userData.toJSON() + ",";
		}
		if (allUserData.size() > 0) response = response.substring(0, response.length() - 1);
		response += "]";
		return response;
	}

	public boolean createUser (String userName, String password) {
		UserData user = getUser(userName);
		if (user != null) {
			Loggers.getLogger(getClass()).error("username " + userName + " is already registered");
			return false;
		} else {
			user = new UserData(userName, password);
			putUser(user);
			return true;
		}
	}

	/**
	 * add permission to an user with a specified index
	 * @param user
	 * @param password
	 * @param path
	 * @return
	 */
	public boolean addAuthIndex (String userName, String indexName) {
		if (indexName != null && indexName.equals("/*")) {
			// root only
			return false;
		}
		UserData user = getUser(userName);
		if (user == null) {
			return false;
		}
		Set<String> indexFilters = user.getIndexFilters();
		if (indexName != null && !indexName.equals("")) {
			String[] indexNames = indexName.split(",");
			for (String index : indexNames) {
				if (index.equals("")) {
					continue;
				}
				if (index.charAt(0) != '/') {
					index = "/" + index;
				}
				indexFilters.add(index);
			}
			user.setFilters(indexFilters);
		}
		return putUser(user);
	}
	
	/**
	 * add permission to an user with a specified index
	 * @param user
	 * @param password
	 * @param path
	 * @return
	 */
	public boolean updateAuthIndex (String userName, String indexName) {
		if (indexName != null && indexName.equals("/*")) {
			// root only
			return false;
		}
		UserData user = getUser(userName);
		if (user == null) {
			return false;
		}
		Set<String> indexFilters = Sets.newCopyOnWriteArraySet();
		if (indexName != null && !indexName.equals("")) {
			String[] indexNames = indexName.split(",");
			for (String index : indexNames) {
				if (index.charAt(0) != '/') {
					index = "/" + index;
				}
				indexFilters.add(index);
			}
		}
		user.setFilters(indexFilters);
		return putUser(user);
	}
	
	/**
	 * add permission to an user with a specified index
	 * @param user
	 * @param password
	 * @param path
	 * @return
	 */
	public boolean removeAuth (String userName, String password, String indexName) {
		UserData user = getUser(userName);
		if (user == null) return false;
		if (user.isValidPassword(password)) {
			Set<String> indices = user.getIndexFilters();
			if (indexName.charAt(0) != '/') {
				indexName = "/" + indexName;
			}
			if (indices.contains(indexName)) {
				indices.remove(indexName);
				putUser(user);
				return true;
			}
		}
		return false;
	}
	
	public boolean changePassword (String userName, String oldPassword, String newPassword) {
		UserData user = getUser(userName);
		if (user == null) return false;
		if (user.isValidPassword(oldPassword)) {
			user.setPassword(newPassword);
			putUser(user);
			return true;
		}
		return false;
	}
	
	private boolean putUser(UserData user) {
		String created = "";
		if (user.getCreated() == null) {
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
	        created = sdf.format(new Date());
		} else {
			created = user.getCreated();
		}
		
		try {
			@SuppressWarnings("unused")
			IndexResponse resp = client.prepareIndex(HTTP_USER_AUTH_INDEX, HTTP_USER_AUTH_TYPE, user.getUsername())
			        .setSource(jsonBuilder()
			                    .startObject()
			                        .field("username", user.getUsername())
			                        .field("password", user.getPassword())
			                        .field("indices", user.getIndexFilters())
			                        .field("created", created)
			                    .endObject()
			                  )
			        .execute()
			        .actionGet();
			reloadUserDataCache();
			return true;
		} catch (ElasticsearchException e) {
			Loggers.getLogger(getClass()).error("ElasticsearchException", e);
		} catch (IOException e) {
			Loggers.getLogger(getClass()).error("IOException", e);
		}
		return false;
	}
	
	private UserData getUser(String userName) {
		GetResponse response = client.prepareGet(HTTP_USER_AUTH_INDEX, HTTP_USER_AUTH_TYPE, userName)
		        .setOperationThreaded(false)
		        .execute()
		        .actionGet();
		if (response.isExists()) {
			Map<String, Object> source = response.getSource();
			return getUserDataFromESSource(source);
		} else {
			return null;
		}
	}

	public boolean deleteUser(String userName) {
		DeleteResponse response = client.prepareDelete(HTTP_USER_AUTH_INDEX, HTTP_USER_AUTH_TYPE, userName)
		        .execute()
		        .actionGet();
		if (response.isFound()) {
			reloadUserDataCache();
			return true;
		} else {
			return false;
		}
	}
	
	private boolean createIndexIfEmpty() {
		IndicesExistsResponse res = client.admin().indices().prepareExists(HTTP_USER_AUTH_INDEX).execute().actionGet();
		if (res.isExists()) {
			return true;
		}
		
		CreateIndexRequest request = new CreateIndexRequest(HTTP_USER_AUTH_INDEX);
		CreateIndexResponse resp = client.admin().indices().create(request).actionGet();
		if (resp.isAcknowledged()) {
			try {
				// wait the creation process has been completed 
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			return true;
		}
		return false;
	}

	private void reloadUserDataCache() {
		List<UserData> userDataList = getAllUserData();
		if (userDataList == null) {
			return ;
		}
		UserAuthenticator.reloadUserDataCache(userDataList);
		isInitialized = true;
	}
	
	/**
	 * get all user info
	 */
	private List<UserData> getAllUserData() {
		try {
			SearchResponse res = client.prepareSearch()
					.setIndices(HTTP_USER_AUTH_INDEX)
					.setTypes(HTTP_USER_AUTH_TYPE)
			        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(QueryBuilders.matchAllQuery())
					.setSize(100)
					.execute()
					.actionGet();
			if (res.getFailedShards() == 0 && res.getHits() != null && res.getHits().hits() != null) {
				List<UserData> userDataList = Lists.newCopyOnWriteArrayList();
				SearchHit[] hits = res.getHits().hits();
				for (int i = 0; i < hits.length; i++) {
					SearchHit hit = hits[i];
					userDataList.add(getUserDataFromESSource(hit.getSource()));
				}
				return userDataList;
			} else {
				Loggers.getLogger(getClass()).error("Failed to get data from some shards");
				return null;
			}
		} catch (Exception ex) {
			// possibly the ES's loading process hasn't finished yet 
			Loggers.getLogger(getClass()).error("failed to load all user info", ex);
		}
		return null;
	}
	
	private UserData getUserDataFromESSource(Map<String, Object> source) {
		String userName = (String)source.get("username");
		String password = (String)source.get("password");
		String created  = (String)source.get("created");
		Set<String> indices;
		if (source.containsKey("indices")) {
			@SuppressWarnings("unchecked")
			List<String> indicesList = (List<String>)source.get("indices");
			indices = Sets.newConcurrentHashSet(indicesList);
		} else {
			indices = Sets.newConcurrentHashSet();
		}
		return UserData.restoreFromESData(userName, password, created, indices);
	}
}

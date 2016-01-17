package org.elasticsearch.plugin.elasticfence.data;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import com.google.common.collect.Sets;

public class UserData {
	private String username;
	private String encPassword;
	private Set<String> indexFilters;
	private String created;
	private UserData() {
		
	}
	public UserData(String userName, String rawPassword) {
		setUsername(userName);
		setPassword(rawPassword);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
		setCreated(sdf.format(new Date()));
		Set<String> indices = Sets.newConcurrentHashSet();
		setFilters(indices);
	}
	public UserData(String userName, String rawPassword, Set<String> filters) {
		setUsername(userName);
		setPassword(rawPassword);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
		setCreated(sdf.format(new Date()));
		if (filters == null) {
			filters = Sets.newConcurrentHashSet();
		}
		setFilters(filters);
	}
	public UserData(String userName, String rawPassword, String... filters) {
		setUsername(userName);
		setPassword(rawPassword);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
		setCreated(sdf.format(new Date()));
		if (filters == null) {
			Set<String> filterSet = Sets.newConcurrentHashSet(Arrays.asList(filters));
			setFilters(filterSet);
		} else {
			Set<String> filterSet = Sets.newConcurrentHashSet();
			setFilters(filterSet);
		}
	}

	public static UserData restoreFromESData(String username, String encPassword, String created, Set<String> indexFilters) {
		UserData user = new UserData();
		user.username = username;
		user.encPassword = encPassword;
		user.created = created;
		user.indexFilters = indexFilters;
		return user;
	}

	public static UserData restoreFromESData(String username, String encPassword, String... indexFilters) {
		UserData user = new UserData();
		user.username = username;
		user.encPassword = encPassword;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
		user.created = sdf.format(new Date());
		user.indexFilters  = Sets.newConcurrentHashSet(Arrays.asList(indexFilters));
		return user;
	}
	
	public static String encPassword(String rawPassword) {
		return DigestUtils.sha256Hex(rawPassword);
	}
	
	public Set<String> getIndexFilters() {
		return indexFilters;
	}

	public void setFilters(Set<String> indexFilters) {
		this.indexFilters = indexFilters;
	}

	public String getPassword() {
		return encPassword;
	}

	public void setPassword(String password) {
		this.encPassword = encPassword(password);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCreated() {
		return created;
	}
	
	public void setCreated(String created) {
		this.created = created;
	}
	
	public boolean isValidPassword(String rawPassword) {
		if (encPassword.equals(encPassword(rawPassword))) {
			return true;
		} else {
			return false;
		}
	}
	
	public String toJSON() {
		if (created == null) {
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
	        created = sdf.format(new Date());
		}
		try {
			return jsonBuilder()
			.startObject()
			    .field("username", username)
			    .field("password", encPassword)
			    .field("indices", indexFilters)
			    .field("created", created)
			.endObject().string();
		} catch (IOException e) {
		}
		return "";
	}
}

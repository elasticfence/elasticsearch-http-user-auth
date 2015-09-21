package org.elasticsearch.plugin.http.user.auth.data;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.elasticsearch.common.collect.Sets;

public class UserData {
	private String username;
	private String encPassword;
	private Set<String> filters;
	private UserData() {
		
	}
	public UserData(String userName, String rawPassword) {
		this.setUsername(userName);
		this.setPassword(rawPassword);
		Set<String> indices = Sets.newConcurrentHashSet();
		this.setFilters(indices);
	}
	public UserData(String userName, String rawPassword, Set<String> filters) {
		this.setUsername(userName);
		this.setPassword(rawPassword);
		if (filters == null) {
			filters = Sets.newConcurrentHashSet();
		}
		this.setFilters(filters);
	}
	public UserData(String userName, String rawPassword, String... filters) {
		this.setUsername(userName);
		this.setPassword(rawPassword);
		if (filters == null) {
			Set<String> filterSet = Sets.newConcurrentHashSet(Arrays.asList(filters));
			this.setFilters(filterSet);
		} else {
			Set<String> filterSet = Sets.newConcurrentHashSet();
			this.setFilters(filterSet);
		}
	}

	public static UserData restoreFromESData(String username, String encPassword, Set<String> filters) {
		UserData user = new UserData();
		user.username = username;
		user.encPassword = encPassword;
		user.filters  = filters;
		return user;
	}

	public static UserData restoreFromESData(String username, String encPassword, String... filters) {
		UserData user = new UserData();
		user.username = username;
		user.encPassword = encPassword;
		user.filters  = Sets.newConcurrentHashSet(Arrays.asList(filters));
		return user;
	}
	
	public static String encPassword(String rawPassword) {
		return DigestUtils.sha256Hex(rawPassword);
	}
	
	public Set<String> getFilters() {
		return filters;
	}

	public void setFilters(Set<String> filters) {
		this.filters = filters;
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
	
	public boolean isValidPassword(String rawPassword) {
		if (encPassword.equals(encPassword(rawPassword))) {
			return true;
		} else {
			return false;
		}
	}
	
	public String toJSON() {
		try {
			return jsonBuilder()
			.startObject()
			    .field("username", username)
			    .field("password", encPassword)
			    .field("indices", filters)
			    .field("created", new Date())
			.endObject().string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}

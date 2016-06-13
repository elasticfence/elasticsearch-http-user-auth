package org.elasticsearch.plugin.elasticfence;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import org.elasticsearch.plugin.elasticfence.data.UserData;
import org.elasticsearch.plugin.elasticfence.logger.EFLogger;
import org.elasticsearch.plugin.elasticfence.parser.RequestParser;

/**
 * A class for checking an index path is accessible by a user. 
 * @author tk
 */
public class UserAuthenticator {
	private static String rootPassword = "";

	// a map of all users' username => UserData 
	private static Map<String, UserData> users;
	private UserData user;
	
	static {
		users = Maps.newConcurrentMap();
	}
	public UserAuthenticator(String username, String rawPassword) {
		if (users.containsKey(username) && users.get(username).isValidPassword(rawPassword)) {
			user = users.get(username);
		}
	}
	public boolean isValidUser() {
		if (user == null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isAccessibleIndices(RequestParser parser) {
		if (user == null) {
			return false;
		}

		if (user.getUsername().equals("root")) {
			return true;
		}
		
		Set<String> filters = user.getIndexFilters();
		String apiName = parser.getApiName();
		List<String> indices = parser.getIndicesInPath();
		if (indices.contains("/*")) {
			// /* is only accessible by root. 
			return false;
		}
		
		switch (apiName) {
			case "_msearch":
				try {
					indices = parser.getIndicesFromMsearchRequestBody();
					return checkIndicesWithFilters(indices, filters);
				} catch (Exception e) {
					EFLogger.error("", e);
				}
				return false;
			case "_mget":
				try {
					indices = parser.getIndicesFromMgetRequestBody();
					return checkIndicesWithFilters(indices, filters);
				} catch (Exception e) {
					EFLogger.error("", e);
				}
				return false;
			case "_bulk":
				try {
					indices = parser.getIndicesFromBulkRequestBody();
					return checkIndicesWithFilters(indices, filters);
				} catch (Exception e) {
					EFLogger.error("", e);
				}
				return false;
			default:
				break;
		}
		
		// check kibana accessibility
		if (isKibanaRequest(parser.getPath()) && isAccessibleUserToKibana(filters)) {
			return true;
		}
		
		// reject if indices contains the empty index ("/") and apiName is not empty
		if (indices.contains("/") && !apiName.equals("")) {
			return false;
		}
		
		// simply compare path indices and index filters
//		if (filters.containsAll(indices)) {
//			return true;
//		}
		return checkIndicesWithFilters(indices, filters);
	}
	
	private boolean checkIndicesWithFilters(List<String> indices, Set<String> filters) {
		for (String index : indices) {
			boolean passed = false;
			for (String filter : filters) {
				if (ifFilterCoversIndex(index, filter)) {
					passed = true;
					break;
				}
			}
			if (!passed) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * check request path if it is used by kibana
	 * @param requestPath
	 * @return
	 */
	private boolean isKibanaRequest(String requestPath) {
		String index = normalizeUrlPath(requestPath);
		if (requestPath.equals("/") || requestPath.equals("/_nodes") || index.equals("/.kibana") || requestPath.equals("/_cluster/health/.kibana")) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * check if an user has auth to kibana
	 * @param filters
	 * @return
	 */
	private boolean isAccessibleUserToKibana(Set<String> filters) {
		if (filters.contains("/.kibana")) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * authenticate a combination of user, password and path
	 * @param path
	 * @param user
	 * @param password
	 * @return
	 */
	public boolean execAuth(String path) {
		if (user == null) {
			return false;
		}
		
		if (user.getUsername().equals("root")) {
			return true;
		}
		
		// the all matching path /* is only accessible by root. 
		if (path.equals("/*")) {
			return false;
		}
		
		String index = normalizeUrlPath(path);
		for (String filter : user.getIndexFilters()) {
			if (ifFilterCoversIndex(index, filter)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * load authentication info when ES instance starts
	 * @param userPassIndices List < Map <key, val>> 
	 */
	public static void loadRootUserDataCacheOnStart() {
		EFLogger.debug("loadRootUserDataCacheOnStart");
		users.put("root", UserData.restoreFromESData("root", rootPassword, "/*"));
	}
	
	/**
	 * reload authentication info
	 * @param userPassIndices List < Map <key, val>> 
	 */
	public static void reloadUserDataCache(List<UserData> userDataList) {
		Map<String, UserData> users  = Maps.newConcurrentMap();
		if (userDataList != null) {
			for (UserData userData : userDataList) {
				users.put(userData.getUsername(), userData);
			}
		}
		// At last, add root user information
		users.put("root", UserData.restoreFromESData("root", rootPassword, "/*"));
		UserAuthenticator.users = users;
	}

	public static void setRootPassword(String rootPassword) {
		if (rootPassword == null) rootPassword = "";
		UserAuthenticator.rootPassword = UserData.encPassword(rootPassword);
	}

	public static String getRootPassword() {
		if (rootPassword == null) return "";
		return rootPassword;
	}
	
	/**
	 * Whether the filter covers the index
	 * When both of them include "*" character, this function just compares them 
	 * as simple strings (not as regex strings) 
	 * @param index
	 * @param filter
	 * @return
	 */
	private boolean ifFilterCoversIndex(String index, String filter) {
		if (index.startsWith("/")) index = index.substring(1);
		if (filter.startsWith("/")) filter = filter.substring(1);
		
		if (index.equals(filter)) return true;
		
		// processing a special case in advance
		if (index.equals("") || filter.equals("")) {
			if (filter.equals("") && index.equals("")) {
				return true;
			}
			return false;
		}
		
		if (!filter.contains("*")) {
			if (index.equals("*")) {
				return true;
			} else {
				// compare as strings
				return index.equals(filter);
			}
		}
		
		// processing regex conditions
		if (index.contains("*")) {
			// just compare if both filter and index include "*" character, too. 
			if (index.equals(filter)) {
				return true;
			} else {
				return false;
			}
		} else {
			// only filter contains "*" char
			String regexStr = "";
			if (!filter.startsWith("*")) regexStr = "^";
			String[] splitStrArr = filter.split("\\*");
			for (int i = 0; i < splitStrArr.length; i++) {
				if (i < splitStrArr.length - 1) {
					if (splitStrArr[i].equals("")) {
						regexStr += ".*?";
					} else {
						regexStr += Pattern.quote(splitStrArr[i]) + ".*?";
					}
				} else {
					regexStr += Pattern.quote(splitStrArr[i]);
				}
			}

			if (filter.endsWith("*")) regexStr += ".*?$";
			else regexStr += "$";
			Pattern p = Pattern.compile(regexStr);
			Matcher m = p.matcher(index);
			if (m.find()){
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * normalizing HTTP URL paths
	 * Ex1: "/test_index/test_type/../../*" => "/*" 
	 * Ex2: "/test_index/test_type/../../../" => "/" 
	 * 
	 * @param path
	 * @return
	 */
	private static String normalizeUrlPath(String path) {
		if (path.equals("") || path.charAt(0) != '/') {
			path = "/" + path;
		}
		try {
			URI uri = URI.create(path);
			uri = uri.normalize();
			path = uri.toString();
		} catch (IllegalArgumentException ex) {
			EFLogger.error("Illegal path: " + path);
			return null;
		} catch (Exception ex) {
			EFLogger.error("invalid path: " + path);
			return null;
		}
		
		// this case won't occur, but just in case
		if (path.equals("")) return "/";
		
		// single slash is special path
		if (path.equals("/")) return "/";
		
		String[] pathInfo = path.split("/");
		String index = "";
		for (String str : pathInfo) {
			// first none-empty string is index name
			if (str.equals("")) continue;
			index = str;
			break;
		}
		
		if (index.startsWith("_")) return "/";
		return "/" + index;
	}

}

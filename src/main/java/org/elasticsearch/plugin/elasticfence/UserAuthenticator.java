package org.elasticsearch.plugin.elasticfence;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.plugin.elasticfence.data.UserData;

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
		ESLoggerFactory.getRootLogger().error("loadRootUserDataCacheOnStart");
		users.put("root", UserData.restoreFromESData("root", rootPassword, "/*"));
	}
	
	/**
	 * reload authentication info
	 * @param userPassIndices List < Map <key, val>> 
	 */
	public static void reloadUserDataCache(List<UserData> userDataList) {
		users  = Maps.newConcurrentMap();
		if (userDataList != null) {
			for (UserData userData : userDataList) {
				users.put(userData.getUsername(), userData);
			}
		}
		// At last, add root user information
		users.put("root", UserData.restoreFromESData("root", rootPassword, "/*"));
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
			if (index.contains("*")) {
				return true;
			} else {
				// just compare if both filter and index are simple strings
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
	 * 最初のスラッシュは削られる
	 * ESのインデックスパスにはスラッシュが必ず含まれるのに…
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
			ESLoggerFactory.getRootLogger().error("Illegal path: " + path);
			return null;
		} catch (Exception ex) {
			ESLoggerFactory.getRootLogger().error("invalid path: " + path);
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

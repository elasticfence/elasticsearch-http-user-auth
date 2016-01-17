package org.elasticsearch.plugin.elasticfence.tool;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;

import org.elasticsearch.rest.RestRequest;

import org.elasticsearch.common.Base64;

public class RequestAnalyzer {
	private String username;
	private String password;
	
	public RequestAnalyzer(RestRequest request) {
		Iterable<Entry<String, String>> headers = request.headers();
		for (Entry<String, String> header : headers) {
			if (header.getKey().toLowerCase().equals("authorization")) {
				String authStr = header.getValue();
				if (authStr == null || authStr.equals("")) {
					return ;
				}

				authStr = authStr.trim();
				String[] authArr = authStr.split(" ");
				for (int i = 1; i < authArr.length; i++) {
					if (authArr[i].equals("")) {
						continue;
					}

				    String userPass = "";
					try {
						userPass = new String(Base64.decode(authArr[i]), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				    String[] userPassArr = userPass.split(":", 2);
				    if (userPassArr.length != 2) {
				    	continue;
				    }
				    username = userPassArr[0];
				    password = userPassArr[1];
				    
				    return;
				}
			}
		}
	}
	
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}

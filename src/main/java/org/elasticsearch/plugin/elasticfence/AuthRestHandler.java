package org.elasticsearch.plugin.elasticfence;

import org.elasticsearch.rest.*;

import net.arnx.jsonic.JSON;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.elasticfence.data.UserDataBridge;
import org.elasticsearch.plugin.elasticfence.logger.ElasticfenceLogger;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.RestStatus.BAD_REQUEST;
import static org.elasticsearch.rest.RestStatus.NOT_IMPLEMENTED;
import static org.elasticsearch.rest.RestStatus.CONFLICT;
import static org.elasticsearch.rest.RestStatus.SERVICE_UNAVAILABLE;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AuthRestHandler extends BaseRestHandler {
    @Inject
    public AuthRestHandler(Settings settings, RestController restController, Client client) {
    	super(settings, restController, client);
        restController.registerHandler(POST, "/_elasticfence", this);
        restController.registerHandler(GET, "/_elasticfence", this);
        RestFilter filter = new AuthRestFilter(client);
        restController.registerFilter(filter);
    }

	@Override
	protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {
		String mode = request.param("mode");
			
		if (mode == null) {
	        channel.sendResponse(new BytesRestResponse(OK, "No Method"));
			return ;
		}
        UserDataBridge userDataBridge = new UserDataBridge(client);
        if (!userDataBridge.isInitialized()) {
	        channel.sendResponse(new BytesRestResponse(SERVICE_UNAVAILABLE, "http user auth initializing..."));
	        return ;
        }
		if (mode.equals("list")) {
	        try {
		        String userListJSON = userDataBridge.listUser();
		        channel.sendResponse(new BytesRestResponse(OK, userListJSON));
	        } catch (Exception ex) {
				ElasticfenceLogger.error("failed to create index: ", ex);
		        channel.sendResponse(new BytesRestResponse(BAD_REQUEST, "failed to list all users"));
	        }
	        return ;
		}

		if (mode.equals("adduser")) {
			String userName = request.param("username");
			String password = request.param("password");
	        try {
		        boolean res = userDataBridge.createUser(userName, password);
		        if (res) {
			        channel.sendResponse(new BytesRestResponse(OK, "User created : " + userName));
		        } else {
			        channel.sendResponse(new BytesRestResponse(CONFLICT, "User already exists : " + userName));
		        }
		        return ;
	        } catch (Exception ex) {
				ElasticfenceLogger.error("failed to create index: ", ex);
		        channel.sendResponse(new BytesRestResponse(BAD_REQUEST, "failed to create index : " + userName));
		        return ;
	        }
		}

		if (mode.equals("addindex")) {
			String userName  = request.param("username");
			String indexName = request.param("index");

	        try {
		        boolean res = userDataBridge.addAuthIndex(userName, indexName);
		        if (res) {
		        	channel.sendResponse(new BytesRestResponse(OK, "added auth index: " + userName));
		        } else {
		        	channel.sendResponse(new BytesRestResponse(BAD_REQUEST, "failed to add auth index: " + userName));
		        }
	        } catch (Exception ex) {
				ElasticfenceLogger.error("failed to add auth index: ", ex);
		        channel.sendResponse(new BytesRestResponse(BAD_REQUEST, "failed to add auth index : " + userName));
		        return ;
	        }
	        return;
		}

		if (mode.equals("updateindex")) {
			String userName  = request.param("username");
			String indexName = request.param("index");

	        try {
		        boolean res = userDataBridge.updateAuthIndex(userName, indexName);
		        if (res) {
		        	channel.sendResponse(new BytesRestResponse(OK, "added auth index: " + userName));
		        } else {
		        	channel.sendResponse(new BytesRestResponse(BAD_REQUEST, "failed to add auth index: " + userName));
		        }
	        } catch (Exception ex) {
				ElasticfenceLogger.error("failed to add auth index: ", ex);
		        channel.sendResponse(new BytesRestResponse(BAD_REQUEST, "failed to add auth index : " + userName));
		        return ;
	        }
	        return;
		}

		if (mode.equals("passwd")) {
			String userName = request.param("username");
			String oldPassword = request.param("old_password");
			String newPassword = request.param("new_password");
	        
	        boolean res = userDataBridge.changePassword(userName, oldPassword, newPassword);
	        if (res) {
	        	channel.sendResponse(new BytesRestResponse(OK, "Password changed : " + userName));
	        } else {
	        	channel.sendResponse(new BytesRestResponse(BAD_REQUEST, "Failed to change Password: " + userName));
	        }
	        return;
		}

		if (mode.equals("deleteuser")) {
			String userName  = request.param("username");
	        
	        boolean res = userDataBridge.deleteUser(userName);
	        if (res) {
	        	channel.sendResponse(new BytesRestResponse(OK, "deleted user: " + userName));
	        } else {
	        	channel.sendResponse(new BytesRestResponse(BAD_REQUEST, "failed to delete user: " + userName));
	        }
	        return;
		}

		if (mode.equals("import_user_data")) {
			try {
				if (request.method().equals(POST)) {
					String boundary = extractBoundaryFromHeader(request);
					if (boundary != null) {
						BytesReference br = request.content();
						String content = br.toUtf8();
						String userDataJson = extractFileContent(content, boundary);
						registerUserFromJson(userDataJson, client);
			        	channel.sendResponse(new BytesRestResponse(OK, "successfully imported user data"));
			        	return ;
					}
				}
			} catch (net.arnx.jsonic.JSONException jex) {
				ElasticfenceLogger.error("", jex);
	        	channel.sendResponse(new BytesRestResponse(BAD_REQUEST, "failed to parse uploaded JSON file"));
	        	return ;
			} catch (Exception ex) {
				ElasticfenceLogger.error("", ex);
			}
        	channel.sendResponse(new BytesRestResponse(BAD_REQUEST, "failed to import user data"));
	        return;
		}

        channel.sendResponse(new BytesRestResponse(NOT_IMPLEMENTED, "unknown mode you specified"));
	}
	
	private String extractBoundaryFromHeader(RestRequest request) {
//		Content-Type multipart/form-data; boundary=---------------------------302572809929485
		String boundary = null;
		Iterator<Map.Entry<String, String>> headers = request.headers().iterator();
		while (headers.hasNext()) {
			Map.Entry<String, String> entry = headers.next();
			if (entry.getKey().toLowerCase().indexOf("content-type") >= 0) {
				String val = entry.getValue();
				if (val.toLowerCase().indexOf("multipart/form-data") >= 0 && val.toLowerCase().indexOf("boundary=") >= 0) {
					String[] boudaryStr = val.split("boundary=");
					if (boudaryStr.length > 1) {
						boundary = boudaryStr[1];
						return boundary;
					}
				}
				break;
			}
		}

		return boundary;
	}
	
	private String extractFileContent(String content, String boundary) {
		boundary = "--" + boundary;
		String[] contents = content.split(boundary);
		for (String c : contents) {
			// Content-Type: text/plain
			if (c.toLowerCase().indexOf("content-type") >= 0 && c.toLowerCase().indexOf("text/plain") >= 0) {
				int pos = c.toLowerCase().indexOf("text/plain") + "text/plain".length();
				return c.substring(pos);
			}
		}
		return "";
	}
	
	@SuppressWarnings("unchecked")
	private void registerUserFromJson(String json, Client client) {
		List<Map<String, Object>> list = JSON.decode(json);
		for (Map<String, Object> map : list) {
			String username = null, encPassword = null;
			List<String> indices = null;
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				switch (entry.getKey()) {
					case "username" :
						username = (String)entry.getValue();
						break;
					case "password" :
						encPassword = (String)entry.getValue();
						break;
					case "indices" :
						indices = (List<String>)entry.getValue();
						break;
					case "created" :
						// don't take over created datetime
				 		break;
					default : 
						break;
				}
			}
			if (username != null && encPassword != null && indices != null) {
				UserDataBridge userDataBridge = new UserDataBridge(client);
				userDataBridge.importUser(username, encPassword, indices);
			}
		}
	}
}
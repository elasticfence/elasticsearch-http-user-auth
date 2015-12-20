package org.elasticsearch.plugin.http.user.auth;

import org.elasticsearch.rest.*;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.http.user.auth.data.UserDataBridge;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.RestStatus.CONFLICT;
import static org.elasticsearch.rest.RestStatus.SERVICE_UNAVAILABLE;

public class AuthRestHandler extends BaseRestHandler {
    @Inject
    public AuthRestHandler(Settings settings, RestController restController, Client client) {
    	super(settings, restController, client);
        restController.registerHandler(GET, "/_httpuserauth", this);
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
				Loggers.getLogger(getClass()).error("failed to create index: ", ex);
		        channel.sendResponse(new BytesRestResponse(OK, "failed to list all users"));
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
				Loggers.getLogger(getClass()).error("failed to create index: ", ex);
		        channel.sendResponse(new BytesRestResponse(OK, "failed to create index : " + userName));
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
		        	channel.sendResponse(new BytesRestResponse(OK, "failed to add auth index: " + userName));
		        }
	        } catch (Exception ex) {
				Loggers.getLogger(getClass()).error("failed to add auth index: ", ex);
		        channel.sendResponse(new BytesRestResponse(OK, "failed to add auth index : " + userName));
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
		        	channel.sendResponse(new BytesRestResponse(OK, "failed to add auth index: " + userName));
		        }
	        } catch (Exception ex) {
				Loggers.getLogger(getClass()).error("failed to add auth index: ", ex);
		        channel.sendResponse(new BytesRestResponse(OK, "failed to add auth index : " + userName));
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
	        	channel.sendResponse(new BytesRestResponse(OK, "Failed to change Password: " + userName));
	        }
	        return;
		}

		if (mode.equals("deleteuser")) {
			String userName  = request.param("username");
	        
	        boolean res = userDataBridge.deleteUser(userName);
	        if (res) {
	        	channel.sendResponse(new BytesRestResponse(OK, "deleted user: " + userName));
	        } else {
	        	channel.sendResponse(new BytesRestResponse(OK, "failed to delete user: " + userName));
	        }
	        return;
		}
        channel.sendResponse(new BytesRestResponse(OK, "Failed"));
	}
}
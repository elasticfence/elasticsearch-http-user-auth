package org.elasticsearch.plugin.elasticfence;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.*;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.elasticfence.data.UserDataBridge;
import org.elasticsearch.plugin.elasticfence.logger.EFLogger;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.RestStatus.CONFLICT;
import static org.elasticsearch.rest.RestStatus.SERVICE_UNAVAILABLE;

public class AuthRestHandler extends BaseRestHandler {
    @Inject
    public AuthRestHandler(Settings settings, RestController restController, Client client) {
        super(settings);
        restController.registerHandler(GET, "/_httpuserauth", this);
        RestFilter filter = new AuthRestFilter(client, settings);
        restController.registerFilter(filter);
    }

	@Override
	protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
		String mode = request.param("mode");
		if (mode == null) {
			return channel -> channel.sendResponse(new BytesRestResponse(OK, "No Method"));
		}

        UserDataBridge userDataBridge = new UserDataBridge(client);
		if (!userDataBridge.isInitialized()) {
			return channel -> channel.sendResponse(new BytesRestResponse(SERVICE_UNAVAILABLE, "http user auth initializing..."));
		}
		if ("list".equals(mode)) {
			try {
				String userListJSON = userDataBridge.listUser();
				return channel -> channel.sendResponse(new BytesRestResponse(OK, userListJSON));
			} catch (Exception ex) {
				EFLogger.error("failed to create index: ", ex);
				return channel -> channel.sendResponse(new BytesRestResponse(OK, "failed to list all users"));
			}
		}
		if ("adduser".equals(mode)) {
			String userName = request.param("username");
			String password = request.param("password");
			try {
				boolean res = userDataBridge.createUser(userName, password);
				if (res) {
					return channel -> channel.sendResponse(new BytesRestResponse(OK, "User created : " + userName));
				} else {
					return channel -> channel.sendResponse(new BytesRestResponse(CONFLICT, "User already exists : " + userName));
				}
			} catch (Exception ex) {
				EFLogger.error("failed to create index: ", ex);
				return channel -> channel.sendResponse(new BytesRestResponse(OK, "failed to create index : " + userName));
			}
		}
		if ("addindex".equals(mode)) {
			String userName  = request.param("username");
			String indexName = request.param("index");

			try {
				boolean res = userDataBridge.addAuthIndex(userName, indexName);
				if (res) {
					return channel -> channel.sendResponse(new BytesRestResponse(OK, "added auth index: " + userName));
				} else {
					return channel -> channel.sendResponse(new BytesRestResponse(OK, "failed to add auth index: " + userName));
				}
			} catch (Exception ex) {
				EFLogger.error("failed to add auth index: ", ex);
				return channel -> channel.sendResponse(new BytesRestResponse(OK, "failed to add auth index : " + userName));
			}
		}

		if ("updateindex".equals(mode)) {
			String userName  = request.param("username");
			String indexName = request.param("index");

			try {
				boolean res = userDataBridge.updateAuthIndex(userName, indexName);
				if (res) {
					return channel -> channel.sendResponse(new BytesRestResponse(OK, "added auth index: " + userName));
				} else {
					return channel -> channel.sendResponse(new BytesRestResponse(OK, "failed to add auth index: " + userName));
				}
			} catch (Exception ex) {
				EFLogger.error("failed to add auth index: ", ex);
				return channel -> channel.sendResponse(new BytesRestResponse(OK, "failed to add auth index : " + userName));
			}
		}

		if ("passwd".equals(mode)) {
			String userName = request.param("username");
			String oldPassword = request.param("old_password");
			String newPassword = request.param("new_password");

			boolean res = userDataBridge.changePassword(userName, oldPassword, newPassword);
			if (res) {
				return channel -> channel.sendResponse(new BytesRestResponse(OK, "Password changed : " + userName));
			} else {
				return channel -> channel.sendResponse(new BytesRestResponse(OK, "Failed to change Password: " + userName));
			}
		}

		if ("deleteuser".equals(mode)) {
			String userName  = request.param("username");

			boolean res = userDataBridge.deleteUser(userName);
			if (res) {
				return channel -> channel.sendResponse(new BytesRestResponse(OK, "deleted user: " + userName));
			} else {
				return channel -> channel.sendResponse(new BytesRestResponse(OK, "failed to delete user: " + userName));
			}
		}
		return channel -> channel.sendResponse(new BytesRestResponse(OK, "Failed"));
	}
}
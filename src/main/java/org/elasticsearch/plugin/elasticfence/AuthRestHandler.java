package org.elasticsearch.plugin.elasticfence;

import org.elasticsearch.rest.*;

import org.elasticsearch.client.node.NodeClient;
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
	//constructor changed in https://github.com/elastic/elasticsearch/pull/15687/files and https://github.com/elastic/elasticsearch/commit/865b951
    @Inject
    public AuthRestHandler(Settings settings, RestController restController, NodeClient client) {
    	super(settings);
        restController.registerHandler(GET, "/_httpuserauth", this);
        RestFilter filter = new AuthRestFilter(settings);
        restController.registerFilter(filter);
    }

	@Override
	protected AuthRestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
		return new AuthRestChannelConsumer(request, client);
	}

	//alternative, look at RestFieldStatsAction.java in ES repo

	private class AuthRestChannelConsumer implements BaseRestHandler.RestChannelConsumer {
		NodeClient client;
		RestRequest request;
		AuthRestChannelConsumer(RestRequest request, NodeClient client) {
			this.request = request;
			this.client = client;
		}

		@Override
		public void accept(RestChannel channel) {
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
			if ("list".equals(mode)) {
				try {
					String userListJSON = userDataBridge.listUser();
					channel.sendResponse(new BytesRestResponse(OK, userListJSON));
				} catch (Exception ex) {
					EFLogger.error("failed to create index: ", ex);
					channel.sendResponse(new BytesRestResponse(OK, "failed to list all users"));
				}
				return ;
			}
			if ("adduser".equals(mode)) {
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
					EFLogger.error("failed to create index: ", ex);
					channel.sendResponse(new BytesRestResponse(OK, "failed to create index : " + userName));
					return ;
				}
			}
			if ("addindex".equals(mode)) {
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
					EFLogger.error("failed to add auth index: ", ex);
					channel.sendResponse(new BytesRestResponse(OK, "failed to add auth index : " + userName));
					return ;
				}
				return;
			}

			if ("updateindex".equals(mode)) {
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
					EFLogger.error("failed to add auth index: ", ex);
					channel.sendResponse(new BytesRestResponse(OK, "failed to add auth index : " + userName));
					return ;
				}
				return;
			}

			if ("passwd".equals(mode)) {
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

			if ("deleteuser".equals(mode)) {
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
}
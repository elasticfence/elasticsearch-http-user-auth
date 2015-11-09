package org.elasticsearch.plugin.http.user.auth;

import java.util.List;
import java.util.Arrays;
import org.elasticsearch.common.logging.Loggers;

public class IPAuthenticator {

        private static String current_ip;
        private static String[] whitelist;
        private static String[] blacklist;

       /**
         * Manage IP whitelist/blacklist
         */

        public void IPAuthenticator() {
		// nothing to see here
        }
        public static void setWhitelist(String[] whitelist) {
                if (whitelist == null) whitelist = new String[]{};
                IPAuthenticator.whitelist = whitelist;
        }

        public boolean isWhitelisted(String ip) {
                if ( Arrays.asList(whitelist).contains(ip) ) {
                        return true;
                } else {
                        return false;
                }
        }
        public static void setBlacklist(String[] blacklist) {
                if (blacklist == null) blacklist = new String[]{};
                IPAuthenticator.blacklist = blacklist;
        }

        public boolean isBlacklisted(String ip) {
                if ( Arrays.asList(blacklist).contains(ip) ) {
                        return true;
                } else {
                        return false;
                }
        }
}



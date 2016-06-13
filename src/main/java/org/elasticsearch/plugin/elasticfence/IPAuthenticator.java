package org.elasticsearch.plugin.elasticfence;

import java.util.Arrays;

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
                if (whitelist == null) 
                    whitelist = new String[]{};
                IPAuthenticator.whitelist = whitelist;
        }

        public boolean isWhitelisted(String ip) {
		        if (whitelist == null) 
		            return false;
                return Arrays.asList(whitelist).contains(ip);
        }
        public static void setBlacklist(String[] blacklist) {
                if (blacklist == null) 
                    blacklist = new String[]{};
                IPAuthenticator.blacklist = blacklist;
        }

        public boolean isBlacklisted(String ip) {
		        if (blacklist == null) 
		            return false;
                return Arrays.asList(blacklist).contains(ip);
        }
}



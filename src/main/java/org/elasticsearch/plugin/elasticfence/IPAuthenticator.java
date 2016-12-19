package org.elasticsearch.plugin.elasticfence;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.net.util.SubnetUtils;

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
                List<String> tmp = new ArrayList<String>();
                for (String str: whitelist) {
                    if (str.contains("/")) {
                        SubnetUtils utils = new SubnetUtils(str);
                        for (String ip: utils.getInfo().getAllAddresses()) {
                            tmp.add(ip);
                        }
                    } else {
                        tmp.add(str);
                    }
                }
                IPAuthenticator.whitelist = tmp.toArray(new String[0]);
        }

        public boolean isWhitelisted(String ip) {
		        if (whitelist == null) 
		            return false;
                return Arrays.asList(whitelist).contains(ip);
        }
        public static void setBlacklist(String[] blacklist) {
                if (blacklist == null) 
                    blacklist = new String[]{};
                List<String> tmp = new ArrayList<String>();
                for (String str: blacklist) {
                    if (str.contains("/")) {
                        SubnetUtils utils = new SubnetUtils(str);
                        for (String ip: utils.getInfo().getAllAddresses()) {
                            tmp.add(ip);
                        }   
                    } else {
                        tmp.add(str);
                    }   
                }   
                IPAuthenticator.blacklist = tmp.toArray(new String[0]);
        }

        public boolean isBlacklisted(String ip) {
		        if (blacklist == null) 
		            return false;
                return Arrays.asList(blacklist).contains(ip);
        }

    public boolean allBlacklisted() {
        if (blacklist == null) {
            return false;
        }

        return Arrays.asList(blacklist).contains("*");
    }
}



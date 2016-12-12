package org.elasticsearch.plugin.elasticfence.logger;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.ESLoggerFactory;

/**
 * simple logger class for elasticfence. 
 * @author TomSearch
 *
 */
public class EFLogger {
	private static Logger esLogger;
	static {
		esLogger = ESLoggerFactory.getLogger("plugin.elasticfence");
	}

	public static void debug(String msg) {
		esLogger.debug(msg);
	}

	public static void info(String msg) {
		esLogger.info(msg);
	}

	public static void warn(String msg) {
		esLogger.warn(msg);
	}

	public static void error(String msg) {
		esLogger.error(msg);
	}
	
	public static void error(String msg, Exception cause) {
		esLogger.error(msg, cause);
	}
}

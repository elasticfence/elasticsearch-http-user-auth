package org.elasticsearch.plugin.elasticfence.parser;

import static org.elasticsearch.common.xcontent.support.XContentMapValues.nodeStringArrayValue;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.plugin.elasticfence.logger.EFLogger;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestActions;

import com.google.common.collect.Lists;

public class RequestParser {
	private RestRequest request;
	private Settings settings;
	private boolean  isInitialized = false;
	private String   apiName;
	private String   path;
	private String   indexInPath;
	private List<String> indicesInPath;
	private String   normalizedPath;
	public RequestParser(RestRequest request, Settings settings) {
		this.path     = request.path();
		this.request  = request;
		this.settings = settings;
	}
	
	private void initialize() {
		if (!isInitialized) {
			normalizedPath = normalizePath(request.path());
			indexInPath    = extractIndexFromPath(normalizedPath);
			indicesInPath  = extractIndicesFromPath(normalizedPath);
			apiName        = extractApiNameFrom(normalizedPath);
			isInitialized  = true;
		}
	}

	public String getPath() {
		return path;
	}
	
	public String getApiName() {
		initialize();
		return apiName;
	}
	
	public List<String> getIndicesInPath() {
		initialize();
		return indicesInPath;
	}
	
    public List<String> getIndicesFromBulkRequestBody() throws Exception {
        boolean allowExplicitIndex = settings.getAsBoolean("rest.action.multi.allow_explicit_index", true);
        BytesReference data = RestActions.getRestContent(request);

    	List<String> indices = new ArrayList<>();
        XContent xContent = XContentFactory.xContent(data);
        int from = 0;
        int length = data.length();
        byte marker = xContent.streamSeparator();
        while (true) {
            int nextMarker = findNextMarker(marker, from, data, length);
            if (nextMarker == -1) {
                break;
            }

            // now parse the action
            try (XContentParser parser = xContent.createParser(data.slice(from, nextMarker - from))) {
                // move pointers
                from = nextMarker + 1;

                // Move to START_OBJECT
                XContentParser.Token token = parser.nextToken();
                if (token == null) {
                    continue;
                }
                assert token == XContentParser.Token.START_OBJECT;
                // Move to FIELD_NAME, that's the action
                token = parser.nextToken();
                assert token == XContentParser.Token.FIELD_NAME;
                String action = parser.currentName();

                String index;
                token = parser.nextToken();

                if (token == XContentParser.Token.START_OBJECT) {
                    String currentFieldName = null;
                    while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                        if (token == XContentParser.Token.FIELD_NAME) {
                            currentFieldName = parser.currentName();
                        } else if (token.isValue()) {
                            if ("_index".equals(currentFieldName)) {
                                if (!allowExplicitIndex) {
                                    throw new IllegalArgumentException("explicit index in bulk is not allowed");
                                }
                                index = "/" + parser.text();
                                if (!indices.contains(index)) {
                                	indices.add(index);
                                }
                            }
                        }
                    }
                }

                if (!"delete".equals(action)) {
                    nextMarker = findNextMarker(marker, from, data, length);
                    if (nextMarker == -1) {
                        break;
                    }
                    // move pointers
                    from = nextMarker + 1;
                }
            }
        }
        return indices;
    }


    public List<String> getIndicesFromMgetRequestBody() throws Exception {
        boolean allowExplicitIndex = settings.getAsBoolean("rest.action.multi.allow_explicit_index", true);
        List<String> indices = new ArrayList<>();
        BytesReference data = RestActions.getRestContent(request);
        try (XContentParser parser = XContentFactory.xContent(data).createParser(data)) {
            XContentParser.Token token;
            String currentFieldName = null;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else if (token == XContentParser.Token.START_ARRAY) {
                    if ("docs".equals(currentFieldName)) {
                    	indices.addAll(parseDocuments(parser, indexInPath, allowExplicitIndex));
                    }
                }
            }
        }
        
        return indices;
    }

    private List<String> parseDocuments(XContentParser parser, @Nullable String defaultIndex, boolean allowExplicitIndex) throws IOException {
        String currentFieldName = null;
        XContentParser.Token token;
        List<String> indices = new ArrayList<>();
        while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
            if (token != XContentParser.Token.START_OBJECT) {
                throw new IllegalArgumentException("docs array element should include an object");
            }
            String index;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else if (token.isValue()) {
                    if ("_index".equals(currentFieldName)) {
                        if (!allowExplicitIndex) {
                            throw new IllegalArgumentException("explicit index in multi get is not allowed");
                        }
                        index = "/" + parser.text();
                        if (!indices.contains(index)) {
                        	indices.add(index);
                        }
                    }
                }
            }
        }
        
        return indices;
    }

	public List<String> getIndicesFromMsearchRequestBody() throws Exception {
		initialize();
        boolean allowExplicitIndex = settings.getAsBoolean("rest.action.multi.allow_explicit_index", true);
		List<String> indexList = parseMsearchRequestBody(RestActions.getRestContent(request), allowExplicitIndex);
	    return indexList;
	}
	
    private List<String> parseMsearchRequestBody(BytesReference data, boolean allowExplicitIndex) throws Exception {
    	List<String> indexList = new ArrayList<>();
        XContent xContent = XContentFactory.xContent(data);
        int from = 0;
        int length = data.length();
        byte marker = xContent.streamSeparator();
        while (true) {
            int nextMarker = findNextMarker(marker, from, data, length);
            if (nextMarker == -1) {
                break;
            }
            // support first line with \n
            if (nextMarker == 0) {
                from = nextMarker + 1;
                continue;
            }
            IndicesOptions defaultOptions = IndicesOptions.strictExpandOpenAndForbidClosed();


            // now parse the action
            if (nextMarker - from > 0) {
                try (XContentParser parser = xContent.createParser(data.slice(from, nextMarker - from))) {
                    Map<String, Object> source = parser.map();
                    for (Map.Entry<String, Object> entry : source.entrySet()) {
                        Object value = entry.getValue();
                        if ("index".equals(entry.getKey()) || "indices".equals(entry.getKey())) {
                            if (!allowExplicitIndex) {
                                throw new IllegalArgumentException("explicit index in multi percolate is not allowed");
                            }
                            for (String index : nodeStringArrayValue(value)) {
                            	index = "/" + index;
                            	if (!indexList.contains(index)) {
                            		indexList.add(index);
                            	}
                            }
                        }
                    }
                    defaultOptions = IndicesOptions.fromMap(source, defaultOptions);
                }
            }

            // move pointers
            from = nextMarker + 1;
            // now for the body
            nextMarker = findNextMarker(marker, from, data, length);
            if (nextMarker == -1) {
                break;
            }
            // move pointers
            from = nextMarker + 1;

        }
        return indexList;
    }

    private static int findNextMarker(byte marker, int from, BytesReference data, int length) {
        for (int i = from; i < length; i++) {
            if (data.get(i) == marker) {
                return i;
            }
        }
        return -1;
    }

	private static List<String> extractIndicesFromPath(String normalizedPath) {
		String[] parts = normalizedPath.split("/");
		String indexStr = "";
		List<String> indices = Lists.newArrayList();
		for (String part : parts) {
			if (part.indexOf('_') == 0) {
				indices.add("/");
				return indices;
			}
			indexStr = part;
			break;
		}
		if (indexStr.indexOf(',') >= 0) {
			for (String index : indexStr.split(",")) {
				index = "/" + index;
				if (!indices.contains(index)) {
					indices.add(index);
				}
			}
		} else {
			indices.add("/" + indexStr);
		}
		return indices;
	}

	private static String extractIndexFromPath(String normalizedPath) {
		String[] parts = normalizedPath.split("/");
		String indexStr = "";
		for (String part : parts) {
			if (part.indexOf('_') == 0) {
				return "/";
			}
			indexStr = part;
			break;
		}
		return indexStr;
	}

	private static String extractApiNameFrom(String normalizedPath) {
		String[] parts = normalizedPath.split("/");
		for (String part : parts) {
			if (part.indexOf('_') == 0) {
				return part;
			}
		}
		return "";
	}
	
	/**
	 * normalizing HTTP URL paths
	 * Ex1: "/test_index/test_type/../../*" => "/*" 
	 * Ex2: "/test_index/test_type/../../../" => "/" 
	 * 
	 * @param path
	 * @return
	 */
	private static String normalizePath(String path) {
		try {
			URI uri = URI.create(path);
			uri = uri.normalize();
			path = uri.toString();
		} catch (IllegalArgumentException ex) {
			EFLogger.error("Illegal path: " + path);
			return "";
		} catch (Exception ex) {
			EFLogger.error("invalid path: " + path);
			return "";
		}
		if (path.charAt(0) == '/') {
			path = path.substring(1);
		}
		return path;
	}
}

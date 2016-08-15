/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.uuf.httpconnector.msf4j;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.msf4j.Request;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UUF HttpRequest implementation based on MSF4J request.
 */
public class MicroserviceHttpRequest implements HttpRequest {

    public static final String PROPERTY_KEY_HTTP_VERSION = "HTTP_VERSION";
    public static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
    private static final Logger log = LoggerFactory.getLogger(MicroserviceHttpRequest.class);

    private final String url;
    private final String method;
    private final String protocol;
    private final String hostName;
    private final Map<String, Cookie> cookies;
    private final Map<String, String> headers;
    private final String uri;
    private final String contextPath;
    private final String uriWithoutContextPath;
    private final String queryString;
    private final Map<String, Object> queryParams;
    private final int contentLength;
    private final Map<String, Object> formParams;
    private final Map<String, Object> files;

    public MicroserviceHttpRequest(Request request) {
        this(request, null);
    }

    public MicroserviceHttpRequest(Request request, MultivaluedMap<String, ?> postParams) {
        this.url = null; // MSF4J Request does not have a 'getUrl()' method.
        this.method = request.getHttpMethod();
        this.protocol = request.getProperty(PROPERTY_KEY_HTTP_VERSION).toString();
        this.headers = request.getHeaders();

        // Process hostname
        String hostHeader = headers.get(HttpHeaders.HOST);
        this.hostName = ((hostHeader == null) ? "localhost" : hostHeader);

        // Process cookies
        String cookieHeader = headers.get(HttpHeaders.COOKIE);
        this.cookies = (cookieHeader == null) ? Collections.emptyMap() :
                ServerCookieDecoder.STRICT.decode(cookieHeader).stream().collect(
                        Collectors.toMap(Cookie::name, c -> c)
                );

        // Process URI
        String rawUri = request.getUri();
        int uriPathEndIndex = rawUri.indexOf('?');
        String rawUriPath, rawQueryString;
        if (uriPathEndIndex == -1) {
            rawUriPath = rawUri;
            rawQueryString = null;
        } else {
            rawUriPath = rawUri.substring(0, uriPathEndIndex);
            rawQueryString = rawUri.substring(uriPathEndIndex + 1, rawUri.length());
        }
        this.uri = QueryStringDecoder.decodeComponent(rawUriPath);
        this.contextPath = HttpRequest.getContextPath(this.uri);
        this.uriWithoutContextPath = HttpRequest.getUriWithoutContextPath(this.uri);
        this.queryString = rawQueryString; // Query string is not very useful, so we don't bother to decode it.
        if (rawQueryString != null) {
            HashMap<String, Object> map = new HashMap<>();
            new QueryStringDecoder(rawQueryString, false).parameters()
                    .forEach((key, value) -> map.put(key, (value.size() == 1) ? value.get(0) : value));
            this.queryParams = map;
        } else {
            this.queryParams = Collections.emptyMap();
        }

        // POST form params
        if (postParams == null) {
            this.formParams = Collections.emptyMap();
            this.files = Collections.emptyMap();
        } else {
            this.formParams = new HashMap<>();
            this.files = new HashMap<>();
            for (Map.Entry<String, ? extends List<?>> entry : postParams.entrySet()) {
                List<?> values = entry.getValue();
                if (values.isEmpty()) {
                    continue;
                }
                if (values.get(0) instanceof File) {
                    this.files.put(entry.getKey(), (values.size() == 1) ? values.get(0) : values);
                } else {
                    this.formParams.put(entry.getKey(), (values.size() == 1) ? values.get(0) : values);
                }
            }
        }

        // Process content length
        String contentLengthHeaderVal = this.headers.get(HTTP_HEADER_CONTENT_LENGTH);
        try {
            this.contentLength = (contentLengthHeaderVal == null) ? 0 : Integer.parseInt(contentLengthHeaderVal);
        } catch (NumberFormatException e) {
            throw new WebApplicationException(
                    "Cannot parse 'Content-Length' header value '" + contentLengthHeaderVal + "' as an integer.", e);
        }
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public String getCookieValue(String cookieName) {
        Cookie cookie = cookies.get(cookieName);
        return (cookie == null) ? null : cookie.value();
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getUriWithoutContextPath() {
        return uriWithoutContextPath;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    @Override
    public String getContentType() {
        return headers.get(HttpHeaders.CONTENT_TYPE);
    }

    @Override
    public Map<String, Object> getFormParams() {
        return formParams;
    }

    @Override
    public Map<String, Object> getFiles() {
        return files;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException("Netty HttpRequest does not have enough information.");
    }

    @Override
    public String getRemoteAddr() {
        throw new UnsupportedOperationException("Netty HttpRequest does not have enough information.");
    }

    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException("Netty HttpRequest does not have enough information.");
    }

    @Override
    public String toString() {
        return "{\"method\": \"" + method + "\", \"protocol\": \"" + protocol + "\", \"uri\": \"" + uri +
                "\", \"queryString\": \"" + queryString + "\"}";
    }
}

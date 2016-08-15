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

import org.apache.commons.io.FilenameUtils;
import org.wso2.carbon.uuf.spi.HttpResponse;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * UUF HttpResponse implementation based on JAX-RS Response.
 */
public class MicroserviceHttpResponse implements HttpResponse {

    private final Map<String, String> headers = new HashMap<>();
    private int status = 200;

    private Object content;
    private String contentType;

    @Override
    public void setStatus(int statusCode) {
        this.status = statusCode;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setContent(String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public void setContent(File content) {
        String extension = FilenameUtils.getExtension(content.getName());
        setContent(content, extension.isEmpty() ? CONTENT_TYPE_WILDCARD : extension);
    }

    @Override
    public void setContent(File content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public void setContent(InputStream content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public void setContent(Object content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public void setContentType(String type) {
        this.contentType = type;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public Response build() {
        Response.ResponseBuilder responseBuilder = Response.status(status);
        if (content != null) {
            responseBuilder.entity(content).type(contentType);
        }
        headers.entrySet().forEach(entry -> responseBuilder.header(entry.getKey(), entry.getValue()));
        return responseBuilder.build();
    }
}

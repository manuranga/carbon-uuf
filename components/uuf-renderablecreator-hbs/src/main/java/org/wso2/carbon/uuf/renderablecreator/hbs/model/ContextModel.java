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

package org.wso2.carbon.uuf.renderablecreator.hbs.model;

import com.github.jknack.handlebars.Context;
import org.wso2.carbon.uuf.api.model.MapModel;

import java.util.HashMap;
import java.util.Map;

public class ContextModel extends MapModel {

    private final Context parentContext;

    public ContextModel(Context context) {
        this(context, new HashMap<>());
    }

    public ContextModel(Context parentContext, Map<String, Object> map) {
        super(map);
        this.parentContext = parentContext;
    }

    public Context getParentContext() {
        return parentContext;
    }
}

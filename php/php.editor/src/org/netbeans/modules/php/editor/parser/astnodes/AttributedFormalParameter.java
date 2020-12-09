/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttributedFormalParameter extends FormalParameter implements Attributed {

    private final List<Attribute> attributes;
    private final FormalParameter parameter;

    public AttributedFormalParameter(int start, int end, List<Attribute> attributes, FormalParameter parameter) {
        super(start, end, parameter.getParameterType(), parameter.getParameterName(), parameter.getDefaultValue(), parameter.isAttributeDeclaration());
        this.attributes = new ArrayList<>();
        this.attributes.addAll(attributes);
        this.parameter = parameter;
    }

    @Override
    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public FormalParameter getOriginal() {
        return parameter;
    }

    @Override
    public String toString() {
        StringBuilder attrs = new StringBuilder();
        getAttributes().forEach(attribute -> attrs.append(attribute).append(' '));
        return attrs.toString() + getOriginal();
    }

}

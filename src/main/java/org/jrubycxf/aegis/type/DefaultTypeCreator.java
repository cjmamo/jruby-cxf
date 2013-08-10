/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Copyright 2013 Claude Mamo
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jrubycxf.aegis.type;

import org.jrubycxf.aegis.DatabindingException;
import org.jrubycxf.aegis.type.basic.BeanType;
import org.jrubycxf.aegis.type.basic.BeanTypeInfo;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

public class DefaultTypeCreator extends AbstractTypeCreator {
    public DefaultTypeCreator() {
    }

    public DefaultTypeCreator(TypeCreationOptions configuration) {
        setConfiguration(configuration);
    }

    @Override
    public org.jrubycxf.aegis.type.TypeClassInfo createClassInfo(Method m, int index) {
        org.jrubycxf.aegis.type.TypeClassInfo info = new org.jrubycxf.aegis.type.TypeClassInfo();
        info.setDescription("method " + m.getName() + " parameter " + index);

        if (index >= 0) {
            info.setType(m.getParameterTypes()[index]);
        } else {
            info.setType(m.getReturnType());
        }

        return info;
    }

    @Override
    public org.jrubycxf.aegis.type.TypeClassInfo createClassInfo(PropertyDescriptor pd) {
        return createBasicClassInfo(pd.getPropertyType());
    }

    @Override
    public org.jrubycxf.aegis.type.AegisType createCollectionType(org.jrubycxf.aegis.type.TypeClassInfo info) {
        if (!(info.getType() instanceof ParameterizedType)) {
            throw new DatabindingException("Cannot create mapping for " + info.getType() 
                                           + ", unspecified component type for " + info.getDescription());
        }

        return createCollectionTypeFromGeneric(info);
    }

    @Override
    public org.jrubycxf.aegis.type.AegisType createDefaultType(org.jrubycxf.aegis.type.TypeClassInfo info) {
        BeanType type = new BeanType();
        /*
         * As of this point, we refuse to do this for generics in general.
         * This might be revisited ... it might turn out to 'just work'.
         */
        Class<?> typeClass = org.jrubycxf.aegis.type.TypeUtil.getTypeClass(info.getType(), false);
        if (typeClass == null) {
            throw new DatabindingException("Unable to map generic type " + info.getType());
        }
        type.setSchemaType(createQName(typeClass));
        type.setTypeClass(typeClass);
        type.setTypeMapping(getTypeMapping());

        BeanTypeInfo typeInfo = type.getTypeInfo();
        typeInfo.setDefaultMinOccurs(getConfiguration().getDefaultMinOccurs());
        typeInfo.setExtensibleAttributes(getConfiguration().isDefaultExtensibleAttributes());
        typeInfo.setExtensibleElements(getConfiguration().isDefaultExtensibleElements());

        return type;
    }
    protected org.jrubycxf.aegis.type.AegisType getOrCreateMapKeyType(org.jrubycxf.aegis.type.TypeClassInfo info) {
        return createObjectType();
    }

    protected AegisType getOrCreateMapValueType(TypeClassInfo info) {
        return createObjectType();
    }
}

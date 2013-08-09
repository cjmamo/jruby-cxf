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
package org.jrubycxf.aegis.type.java5;

import java.lang.reflect.Type;
import java.util.List;

import org.jrubycxf.aegis.Context;
import org.jrubycxf.aegis.DatabindingException;
import org.jrubycxf.aegis.type.AegisType;
import org.jrubycxf.aegis.xml.MessageReader;
import org.jrubycxf.aegis.xml.MessageWriter;
import org.apache.cxf.common.xmlschema.XmlSchemaConstants;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;

public class EnumType extends AegisType {
    @SuppressWarnings("unchecked")
    @Override
    public Object readObject(MessageReader reader, Context context) {
        String value = reader.getValue();
        @SuppressWarnings("rawtypes")
        Class<? extends Enum> cls = (Class<? extends Enum>)getTypeClass();
        return Enum.valueOf(cls, value.trim());
    }

    @Override
    public void writeObject(Object object, MessageWriter writer, Context context) {
        // match the reader.
        writer.writeValue(((Enum<?>)object).name());
    }

    @Override
    public void setTypeClass(Type typeClass) {
        if (!(typeClass instanceof Class)) {
            throw new DatabindingException("Aegis cannot map generic Enums.");
        }

        Class<?> plainClass = (Class<?>)typeClass;
        if (!plainClass.isEnum()) {
            throw new DatabindingException("EnumType must map an enum.");
        }

        super.setTypeClass(typeClass);
    }

    @Override
    public void writeSchema(XmlSchema root) {

        XmlSchemaSimpleType simple = new XmlSchemaSimpleType(root, true);
        simple.setName(getSchemaType().getLocalPart());
        XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
        restriction.setBaseTypeName(XmlSchemaConstants.STRING_QNAME);
        simple.setContent(restriction);

        Object[] constants = getTypeClass().getEnumConstants();

        List<XmlSchemaFacet> facets = restriction.getFacets();
        for (Object constant : constants) {
            XmlSchemaEnumerationFacet f = new XmlSchemaEnumerationFacet();
            f.setValue(((Enum<?>)constant).name());
            facets.add(f);
        }
    }

    @Override
    public boolean isComplex() {
        return true;
    }
}

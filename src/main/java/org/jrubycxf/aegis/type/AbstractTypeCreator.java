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

import org.apache.cxf.common.WSDLConstants;
import org.apache.cxf.common.util.XMLSchemaQNames;
import org.jrubycxf.aegis.DatabindingException;
import org.jrubycxf.aegis.type.basic.ArrayType;
import org.jrubycxf.aegis.type.basic.ObjectType;
import org.jrubycxf.aegis.type.collection.CollectionType;
import org.jrubycxf.aegis.type.collection.MapType;
import org.jrubycxf.aegis.util.NamespaceHelper;
import org.jrubycxf.aegis.util.ServiceUtils;

import javax.xml.namespace.QName;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;

public abstract class AbstractTypeCreator implements org.jrubycxf.aegis.type.TypeCreator {
    public static final String HTTP_CXF_APACHE_ORG_ARRAYS = "http://cxf.apache.org/arrays";

    protected org.jrubycxf.aegis.type.TypeMapping tm;

    protected AbstractTypeCreator nextCreator;

    private TypeCreationOptions typeConfiguration;

    private org.jrubycxf.aegis.type.TypeCreator parent;

    public org.jrubycxf.aegis.type.TypeMapping getTypeMapping() {
        return tm;
    }

    public org.jrubycxf.aegis.type.TypeCreator getTopCreator() {
        org.jrubycxf.aegis.type.TypeCreator top = this;
        org.jrubycxf.aegis.type.TypeCreator next = top;
        while (next != null) {
            top = next;
            next = top.getParent();
        }
        return top;
    }

    public org.jrubycxf.aegis.type.TypeCreator getParent() {
        return parent;
    }

    public void setParent(org.jrubycxf.aegis.type.TypeCreator parent) {
        this.parent = parent;
    }

    public void setTypeMapping(TypeMapping typeMapping) {
        this.tm = typeMapping;

        if (nextCreator != null) {
            nextCreator.setTypeMapping(tm);
        }
    }

    public void setNextCreator(AbstractTypeCreator creator) {
        this.nextCreator = creator;
        nextCreator.parent = this;
    }

    public org.jrubycxf.aegis.type.TypeClassInfo createClassInfo(Field f) {
        org.jrubycxf.aegis.type.TypeClassInfo info = createBasicClassInfo(f.getType());
        info.setDescription("field " + f.getName() + " in  " + f.getDeclaringClass());
        return info;
    }
    
    public org.jrubycxf.aegis.type.TypeClassInfo createBasicClassInfo(Type type) {
        org.jrubycxf.aegis.type.TypeClassInfo info = new org.jrubycxf.aegis.type.TypeClassInfo();
        Class<?> typeClass = org.jrubycxf.aegis.type.TypeUtil.getTypeClass(type, false);
        if (typeClass != null) {
            info.setDescription("class '" + typeClass.getName() + "'");
        } else {
            info.setDescription("type '" + type + "'");
        }
        info.setType(type);

        return info;
    }

    public org.jrubycxf.aegis.type.AegisType createTypeForClass(org.jrubycxf.aegis.type.TypeClassInfo info) {
        
        Class<?> javaClass = org.jrubycxf.aegis.type.TypeUtil.getTypeRelatedClass(info.getType());
        org.jrubycxf.aegis.type.AegisType result = null;
        boolean newType = true;
        if (info.getType() instanceof TypeVariable) {
            //it's the generic type
            result = getOrCreateGenericType(info);
        } else if (info.getAegisTypeClass() != null) {
            result = createUserType(info);
        } else if (isArray(javaClass)) {
            result = createArrayType(info);
        } else if (isMap(javaClass)) {
            result = createMapType(info);
        }  else if (isHolder(javaClass)) {
            result = createHolderType(info);
        } else if (isCollection(javaClass)) {
            result = createCollectionType(info);
        } else if (isEnum(javaClass)) {
            result = createEnumType(info);
        } else if (javaClass.equals(byte[].class)) {
            result = getTypeMapping().getType(javaClass);
        } else {
            org.jrubycxf.aegis.type.AegisType type = getTypeMapping().getType(info.getType());
            if (type == null 
                || (info.getTypeName() != null && !type.getSchemaType().equals(info.getTypeName()))) {
                if (info.getTypeName() != null) {
                    type = getTypeMapping().getType(info.getTypeName());
                }
                if (type == null) {
                    type = getTypeMapping().getType(javaClass);
                }
                if (type == null) {
                    type = createDefaultType(info);
                } else {
                    newType = false;
                }
            } else {
                newType = false;
            }

            result = type;
        }

        if (newType
            && !getConfiguration().isDefaultNillable()) {
            result.setNillable(false);
        }

        return result;
    }


    protected boolean isHolder(Class<?> javaType) {
        return "javax.xml.ws.Holder".equals(javaType.getName());
    }

    protected org.jrubycxf.aegis.type.AegisType createHolderType(org.jrubycxf.aegis.type.TypeClassInfo info) {

        Type heldType = org.jrubycxf.aegis.type.TypeUtil.getSingleTypeParameter(info.getType(), 0);
        if (heldType == null) {
            throw new UnsupportedOperationException("Invalid holder type " + info.getType());
        }

        info.setType(heldType);
        return createType(heldType);
    }


    protected boolean isArray(Class<?> javaType) {
        return javaType.isArray() && !javaType.equals(byte[].class);
    }

    protected org.jrubycxf.aegis.type.AegisType createUserType(org.jrubycxf.aegis.type.TypeClassInfo info) {
        try {
            org.jrubycxf.aegis.type.AegisType type = info.getAegisTypeClass().newInstance();

            QName name = info.getTypeName();
            if (name == null) {
                // We do not want to use the java.lang.whatever schema type.
                // If the @ annotation or XML file didn't specify a schema type,
                // but the natural type has a schema type mapping, we use that rather
                // than create nonsense.
                Class<?> typeClass = org.jrubycxf.aegis.type.TypeUtil.getTypeRelatedClass(info.getType());
                if (typeClass.getPackage().getName().startsWith("java")) {
                    name = tm.getTypeQName(typeClass);
                }
                // if it's still null, we'll take our lumps, but probably end up with
                // an invalid schema.
                if (name == null) {
                    name = createQName(typeClass);
                }
            }

            type.setSchemaType(name);
            type.setTypeClass(info.getType());
            type.setTypeMapping(getTypeMapping());

            return type;
        } catch (InstantiationException e) {
            throw new DatabindingException("Couldn't instantiate type classs " 
                                           + info.getAegisTypeClass().getName(), e);
        } catch (IllegalAccessException e) {
            throw new DatabindingException("Couldn't access type classs " 
                                           + info.getAegisTypeClass().getName(), e);
        }
    }

    protected org.jrubycxf.aegis.type.AegisType createArrayType(org.jrubycxf.aegis.type.TypeClassInfo info) {
        ArrayType type = new ArrayType();
        type.setTypeMapping(getTypeMapping());
        type.setTypeClass(info.getType());
        type.setSchemaType(createCollectionQName(info, type.getComponentType()));

        if (info.getMinOccurs() != -1) {
            type.setMinOccurs(info.getMinOccurs());
        } else {
            type.setMinOccurs(typeConfiguration.getDefaultMinOccurs());
        }
        
        if (info.getMaxOccurs() != -1) {
            type.setMaxOccurs(info.getMaxOccurs());
        }
        
        type.setFlat(info.isFlat());

        return type;
    }

    protected QName createQName(Class<?> javaType) {
        String clsName = javaType.getName();

        String ns = NamespaceHelper.makeNamespaceFromClassName(clsName, "http");
        String localName = ServiceUtils.makeServiceNameFromClassName(javaType);

        return new QName(ns, localName);
    }

    protected boolean isCollection(Class<?> javaType) {
        return Collection.class.isAssignableFrom(javaType);
    }

    protected org.jrubycxf.aegis.type.AegisType createCollectionTypeFromGeneric(org.jrubycxf.aegis.type.TypeClassInfo info) {
        org.jrubycxf.aegis.type.AegisType component = getOrCreateGenericType(info);

        CollectionType type = new CollectionType(component);
        type.setTypeMapping(getTypeMapping());

        QName name = info.getTypeName();
        if (name == null) {
            name = createCollectionQName(info, component);
        }

        type.setSchemaType(name);

        type.setTypeClass(info.getType());

        if (info.getMinOccurs() != -1) {
            type.setMinOccurs(info.getMinOccurs());
        }
        if (info.getMaxOccurs() != -1) {
            type.setMaxOccurs(info.getMaxOccurs());
        }
        
        type.setFlat(info.isFlat());

        return type;
    }

    protected org.jrubycxf.aegis.type.AegisType getOrCreateGenericType(org.jrubycxf.aegis.type.TypeClassInfo info) {
        return createObjectType();
    }

    protected org.jrubycxf.aegis.type.AegisType getOrCreateMapKeyType(org.jrubycxf.aegis.type.TypeClassInfo info) {
        return nextCreator.getOrCreateMapKeyType(info);
    }

    protected org.jrubycxf.aegis.type.AegisType createObjectType() {
        ObjectType type = new ObjectType();
        type.setSchemaType(XMLSchemaQNames.XSD_ANY);
        type.setTypeClass(Object.class);
        type.setTypeMapping(getTypeMapping());
        return type;
    }

    protected org.jrubycxf.aegis.type.AegisType getOrCreateMapValueType(org.jrubycxf.aegis.type.TypeClassInfo info) {
        return nextCreator.getOrCreateMapValueType(info);
    }

    protected org.jrubycxf.aegis.type.AegisType createMapType(org.jrubycxf.aegis.type.TypeClassInfo info, org.jrubycxf.aegis.type.AegisType keyType, org.jrubycxf.aegis.type.AegisType valueType) {
        QName schemaType = createMapQName(info, keyType, valueType);
        MapType type = new MapType(schemaType, keyType, valueType);
        type.setTypeMapping(getTypeMapping());
        type.setTypeClass(info.getType());

        return type;
    }

    protected org.jrubycxf.aegis.type.AegisType createMapType(org.jrubycxf.aegis.type.TypeClassInfo info) {
        org.jrubycxf.aegis.type.AegisType keyType = getOrCreateMapKeyType(info);
        org.jrubycxf.aegis.type.AegisType valueType = getOrCreateMapValueType(info);

        return createMapType(info, keyType, valueType);
    }

    protected QName createMapQName(org.jrubycxf.aegis.type.TypeClassInfo info, org.jrubycxf.aegis.type.AegisType keyType, org.jrubycxf.aegis.type.AegisType valueType) {
        String name = keyType.getSchemaType().getLocalPart() + '2' + valueType.getSchemaType().getLocalPart();
        
        
        Class<?> cls = TypeUtil.getTypeRelatedClass(info.getType());
        name += cls.getSimpleName();

        // TODO: Get namespace from XML?
        return new QName(tm.getMappingIdentifierURI(), name);
    }

    protected boolean isMap(Class<?> javaType) {
        return Map.class.isAssignableFrom(javaType);
    }

    public abstract org.jrubycxf.aegis.type.TypeClassInfo createClassInfo(PropertyDescriptor pd);

    protected boolean isEnum(Class<?> javaType) {
        return false;
    }

    public org.jrubycxf.aegis.type.AegisType createEnumType(org.jrubycxf.aegis.type.TypeClassInfo info) {
        return null;
    }

    public abstract org.jrubycxf.aegis.type.AegisType createCollectionType(org.jrubycxf.aegis.type.TypeClassInfo info);

    public abstract org.jrubycxf.aegis.type.AegisType createDefaultType(org.jrubycxf.aegis.type.TypeClassInfo info);

    protected QName createCollectionQName(org.jrubycxf.aegis.type.TypeClassInfo info, org.jrubycxf.aegis.type.AegisType type) {
        String ns;

        if (type.isComplex()) {
            ns = type.getSchemaType().getNamespaceURI();
        } else {
            ns = tm.getMappingIdentifierURI();
        }
        if (WSDLConstants.NS_SCHEMA_XSD.equals(ns)) {
            ns = HTTP_CXF_APACHE_ORG_ARRAYS;
        }

        String first = type.getSchemaType().getLocalPart().substring(0, 1);
        String last = type.getSchemaType().getLocalPart().substring(1);
        String localName = "ArrayOf" + first.toUpperCase() + last;
        if (info.nonDefaultAttributes()) {
            localName += "-";
            if (info.getMinOccurs() >= 0) {
                localName += info.getMinOccurs();
            }
            localName += "-";
            if (info.getMaxOccurs() >= 0) {
                localName += info.getMaxOccurs();
            }
            if (info.isFlat()) {
                localName += "Flat";
            }
        }

        return new QName(ns, localName);
    }

    public abstract org.jrubycxf.aegis.type.TypeClassInfo createClassInfo(Method m, int index);

    /**
     * Create a AegisType for a Method parameter.
     * 
     * @param m the method to create a type for
     * @param index The parameter index. If the index is less than zero, the
     *            return type is used.
     */
    public org.jrubycxf.aegis.type.AegisType createType(Method m, int index) {
        org.jrubycxf.aegis.type.TypeClassInfo info = createClassInfo(m, index);
        info.setDescription((index == -1 ? "return type" : "parameter " + index) + " of method "
                            + m.getName() + " in " + m.getDeclaringClass());
        return createTypeForClass(info);
    }

    public QName getElementName(Method m, int index) {
        org.jrubycxf.aegis.type.TypeClassInfo info = createClassInfo(m, index);

        return info.getMappedName();
    }

    /**
     * Create type information for a PropertyDescriptor.
     * 
     * @param pd the propertydescriptor
     */
    public org.jrubycxf.aegis.type.AegisType createType(PropertyDescriptor pd) {
        org.jrubycxf.aegis.type.TypeClassInfo info = createClassInfo(pd);
        info.setDescription("property " + pd.getName());
        return createTypeForClass(info);
    }

    /**
     * Create type information for a <code>Field</code>.
     * 
     * @param f the field to create a type from
     */
    public org.jrubycxf.aegis.type.AegisType createType(Field f) {
        org.jrubycxf.aegis.type.TypeClassInfo info = createClassInfo(f);
        info.setDescription("field " + f.getName() + " in " + f.getDeclaringClass());
        return createTypeForClass(info);
    }
    
    /**
     * Create an Aegis type from a reflected type description.
     * This will only work for the restricted set of collection
     * types supported by Aegis. 
     * @param t the reflected type.
     * @return the type
     */
    public org.jrubycxf.aegis.type.AegisType createType(Type t) {
        org.jrubycxf.aegis.type.TypeClassInfo info = new org.jrubycxf.aegis.type.TypeClassInfo();
        info.setType(t);
        info.setDescription("reflected type " + t.toString());
        return createTypeForClass(info);
        
    }

    public AegisType createType(Class<?> clazz) {
        TypeClassInfo info = createBasicClassInfo(clazz);
        info.setDescription(clazz.toString());
        return createTypeForClass(info);
    }

    public TypeCreationOptions getConfiguration() {
        return typeConfiguration;
    }

    public void setConfiguration(TypeCreationOptions tpConfiguration) {
        this.typeConfiguration = tpConfiguration;
    }
}

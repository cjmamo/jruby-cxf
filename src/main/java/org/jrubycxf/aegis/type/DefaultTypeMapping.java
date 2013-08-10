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

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.SOAPConstants;
import org.apache.cxf.common.util.XMLSchemaQNames;
import org.jrubycxf.aegis.type.basic.*;
import org.jrubycxf.aegis.type.mtom.AbstractXOPType;
import org.jrubycxf.aegis.type.mtom.DataHandlerType;
import org.jrubycxf.aegis.type.mtom.DataSourceType;
import org.jrubycxf.aegis.type.xml.*;
import org.w3c.dom.Document;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Logger;

/**
 * The implementation of the Aegis type map. It maintains a map from
 * Java types {@link java.lang.reflect.Type} and AegisType objects,
 * also indexed by the XML Schema QName of each type.
 */
public class DefaultTypeMapping implements org.jrubycxf.aegis.type.TypeMapping {
    public  static final String DEFAULT_MAPPING_URI = "urn:org.jrubycxf.aegis.types";
    private static final Logger LOG = LogUtils.getL7dLogger(DefaultTypeMapping.class);
    private Map<Type, org.jrubycxf.aegis.type.AegisType> class2Type;
    private Map<QName, org.jrubycxf.aegis.type.AegisType> xml2Type;
    private Map<Type, QName> class2xml;
    private org.jrubycxf.aegis.type.TypeMapping nextTM;
    private org.jrubycxf.aegis.type.TypeCreator typeCreator;
    private String identifierURI;

    public DefaultTypeMapping(String identifierURI, org.jrubycxf.aegis.type.TypeMapping defaultTM) {
        this(identifierURI);

        this.nextTM = defaultTM;
    }
    
    public DefaultTypeMapping() {
        this(DEFAULT_MAPPING_URI);
    }

    public DefaultTypeMapping(String identifierURI) {
        this.identifierURI = identifierURI == null ? DEFAULT_MAPPING_URI : identifierURI;
        class2Type = Collections.synchronizedMap(new HashMap<Type, org.jrubycxf.aegis.type.AegisType>());
        class2xml = Collections.synchronizedMap(new HashMap<Type, QName>());
        xml2Type = Collections.synchronizedMap(new HashMap<QName, org.jrubycxf.aegis.type.AegisType>());
    }

    public boolean isRegistered(Type javaType) {
        boolean registered = class2Type.containsKey(javaType);

        if (!registered && nextTM != null) {
            registered = nextTM.isRegistered(javaType);
        }

        return registered;
    }

    public boolean isRegistered(QName xmlType) {
        boolean registered = xml2Type.containsKey(xmlType);

        if (!registered && nextTM != null) {
            registered = nextTM.isRegistered(xmlType);
        }

        return registered;
    }

    public void register(Type javaType, QName xmlType, org.jrubycxf.aegis.type.AegisType type) {
        type.setSchemaType(xmlType);
        type.setTypeClass(javaType);

        register(type);
    }

    /**
     * {@inheritDoc}
     */
    public void register(org.jrubycxf.aegis.type.AegisType type) {
        type.setTypeMapping(this);
        if (type.getType() != null) {
            class2xml.put(type.getType(), type.getSchemaType());
            class2Type.put(type.getType(), type);
        }
        if (type.getSchemaType() != null) {
            xml2Type.put(type.getSchemaType(), type);
        }
        if (type.getType() == null && type.getSchemaType() == null) {
            LOG.warning("The type " + type.getClass().getName()
                     + " supports neither serialization (non-null TypeClass)"
                     + " nor deserialization (non-null SchemaType).");
        }
    }

    public void removeType(org.jrubycxf.aegis.type.AegisType type) {
        if (!xml2Type.containsKey(type.getSchemaType())) {
            nextTM.removeType(type);
        } else {
            xml2Type.remove(type.getSchemaType());
            class2Type.remove(type.getType());
            class2xml.remove(type.getType());
        }
    }

    public org.jrubycxf.aegis.type.AegisType getType(Type javaType) {
        org.jrubycxf.aegis.type.AegisType type = class2Type.get(javaType);

        if (type == null && nextTM != null) {
            type = nextTM.getType(javaType);
        }

        return type;
    }

    public org.jrubycxf.aegis.type.AegisType getType(QName xmlType) {
        org.jrubycxf.aegis.type.AegisType type = xml2Type.get(xmlType);

        if (type == null && nextTM != null) {
            type = nextTM.getType(xmlType);
        }

        return type;
    }

    public QName getTypeQName(Type clazz) {
        QName qname = class2xml.get(clazz);

        if (qname == null && nextTM != null) {
            qname = nextTM.getTypeQName(clazz);
        }

        return qname;
    }

    public org.jrubycxf.aegis.type.TypeCreator getTypeCreator() {
        return typeCreator;
    }

    public void setTypeCreator(org.jrubycxf.aegis.type.TypeCreator typeCreator) {
        this.typeCreator = typeCreator;

        typeCreator.setTypeMapping(this);
    }

    public org.jrubycxf.aegis.type.TypeMapping getParent() {
        return nextTM;
    }

    private static void defaultRegister(org.jrubycxf.aegis.type.TypeMapping tm, boolean defaultNillable, Class<?> class1,
                                        QName name,
                                        AegisType type) {
        if (!defaultNillable) {
            type.setNillable(false);
        }

        tm.register(class1, name, type);
    }

    private static void fillStandardMappings(org.jrubycxf.aegis.type.TypeMapping tm, boolean defaultNillable,
                                             boolean enableMtomXmime, boolean enableJDOM) {
        defaultRegister(tm, defaultNillable, BigDecimal.class, XMLSchemaQNames.XSD_DECIMAL,
                        new BigDecimalType());
        defaultRegister(tm, defaultNillable, BigInteger.class, XMLSchemaQNames.XSD_INTEGER,
                        new BigIntegerType());
        defaultRegister(tm, defaultNillable, Boolean.class, XMLSchemaQNames.XSD_BOOLEAN,
                        new BooleanType());
        defaultRegister(tm, defaultNillable, Calendar.class, XMLSchemaQNames.XSD_DATETIME,
                        new CalendarType());
        defaultRegister(tm, defaultNillable, Date.class, XMLSchemaQNames.XSD_DATETIME, new DateTimeType());
        defaultRegister(tm, defaultNillable, Float.class, XMLSchemaQNames.XSD_FLOAT, new FloatType());
        defaultRegister(tm, defaultNillable, Double.class, XMLSchemaQNames.XSD_DOUBLE, new DoubleType());
        defaultRegister(tm, defaultNillable, Integer.class, XMLSchemaQNames.XSD_INT, new IntType());
        defaultRegister(tm, defaultNillable, Long.class, XMLSchemaQNames.XSD_LONG, new LongType());
        defaultRegister(tm, defaultNillable, Object.class, XMLSchemaQNames.XSD_ANY, new ObjectType());
        defaultRegister(tm, defaultNillable, Byte.class, XMLSchemaQNames.XSD_BYTE, new ByteType());
        defaultRegister(tm, defaultNillable, Short.class, XMLSchemaQNames.XSD_SHORT, new ShortType());
        defaultRegister(tm, defaultNillable, Source.class, XMLSchemaQNames.XSD_ANY, new SourceType());
        defaultRegister(tm, defaultNillable, String.class, XMLSchemaQNames.XSD_STRING, new StringType());
        defaultRegister(tm, defaultNillable, Time.class, XMLSchemaQNames.XSD_TIME, new TimeType());
        defaultRegister(tm, defaultNillable, Timestamp.class, XMLSchemaQNames.XSD_DATETIME,
                        new TimestampType());
        defaultRegister(tm, defaultNillable, URI.class, XMLSchemaQNames.XSD_URI, new URIType());
        defaultRegister(tm, defaultNillable, XMLStreamReader.class, XMLSchemaQNames.XSD_ANY,
                        new XMLStreamReaderType());
        
        defaultRegister(tm, defaultNillable, boolean.class, XMLSchemaQNames.XSD_BOOLEAN,
                        new BooleanType());
        defaultRegister(tm, defaultNillable, byte[].class, XMLSchemaQNames.XSD_BASE64, new Base64Type());
        defaultRegister(tm, defaultNillable, double.class, XMLSchemaQNames.XSD_DOUBLE, new DoubleType());
        defaultRegister(tm, defaultNillable, float.class, XMLSchemaQNames.XSD_FLOAT, new FloatType());
        defaultRegister(tm, defaultNillable, int.class, XMLSchemaQNames.XSD_INT, new IntType());
        defaultRegister(tm, defaultNillable, short.class, XMLSchemaQNames.XSD_SHORT, new ShortType());
        defaultRegister(tm, defaultNillable, byte.class, XMLSchemaQNames.XSD_BYTE, new ByteType());
        defaultRegister(tm, defaultNillable, long.class, XMLSchemaQNames.XSD_LONG, new LongType());

        defaultRegister(tm, defaultNillable, java.sql.Date.class, XMLSchemaQNames.XSD_DATETIME,
                        new SqlDateType());
        defaultRegister(tm, defaultNillable, java.sql.Date.class, XMLSchemaQNames.XSD_DATE,
                        new SqlDateType());
        defaultRegister(tm, defaultNillable, Number.class, XMLSchemaQNames.XSD_DECIMAL,
                        new BigDecimalType());
        
        QName mtomBase64 = XMLSchemaQNames.XSD_BASE64;
        if (enableMtomXmime) {
            mtomBase64 = AbstractXOPType.XML_MIME_BASE64;
        }

        defaultRegister(tm, defaultNillable, DataSource.class, mtomBase64,
                        new DataSourceType(enableMtomXmime, null));
        defaultRegister(tm, defaultNillable, DataHandler.class, mtomBase64,
                        new DataHandlerType(enableMtomXmime, null));
        

        defaultRegister(tm, defaultNillable, Document.class, XMLSchemaQNames.XSD_ANY, new DocumentType());
        if (enableJDOM) {
            registerJDOMTypes(tm, defaultNillable);
        }

    }

    private static void registerJDOMTypes(TypeMapping tm, boolean defaultNillable) {
        try {
            Class<?> jdomDocClass = ClassLoaderUtils.loadClass("org.jdom.Document", DefaultTypeMapping.class);
            defaultRegister(tm, defaultNillable, jdomDocClass, XMLSchemaQNames.XSD_ANY,
                            new JDOMDocumentType());

        } catch (ClassNotFoundException e) {
            // not available.
        }
        
        try {
            Class<?> jdomElementClass = 
                ClassLoaderUtils.loadClass("org.jdom.Element", DefaultTypeMapping.class);
            defaultRegister(tm, defaultNillable, jdomElementClass, XMLSchemaQNames.XSD_ANY,
                                new JDOMElementType());
        } catch (ClassNotFoundException e) {
            // not available.
        }
    }

    public static DefaultTypeMapping createSoap11TypeMapping(boolean defaultNillable, 
     boolean enableMtomXmime) {
        return createSoap11TypeMapping(
                                       defaultNillable,
                                       enableMtomXmime,
                                       false);
    }

    /**
     * Create a type mapping object with a stock set of mappings, including the SOAP 1.1 'encoded'
     * types.
     * @param defaultNillable whether elements are nillable by default.
     * @param enableMtomXmime whether to enable XMIME annotations with MTOM.
     * @param enableJDOM whether to add mappings for JDOM.
     * @return
     */
    public static DefaultTypeMapping createSoap11TypeMapping(boolean defaultNillable, 
                                                             boolean enableMtomXmime, boolean enableJDOM) {
        // Create a AegisType Mapping for SOAP 1.1 Encoding
        DefaultTypeMapping soapTM = new DefaultTypeMapping(Soap11.SOAP_ENCODING_URI);
        fillStandardMappings(soapTM, defaultNillable, enableMtomXmime, enableJDOM);

        defaultRegister(soapTM, defaultNillable, boolean.class, Soap11.ENCODED_BOOLEAN, new BooleanType());
        defaultRegister(soapTM, defaultNillable, char.class, Soap11.ENCODED_CHAR, new CharacterType());
        defaultRegister(soapTM, defaultNillable, int.class, Soap11.ENCODED_INT, new IntType());
        defaultRegister(soapTM, defaultNillable, short.class, Soap11.ENCODED_SHORT, new ShortType());
        defaultRegister(soapTM, defaultNillable, double.class, Soap11.ENCODED_DOUBLE, new DoubleType());
        defaultRegister(soapTM, defaultNillable, float.class, Soap11.ENCODED_FLOAT, new FloatType());
        defaultRegister(soapTM, defaultNillable, long.class, Soap11.ENCODED_LONG, new LongType());
        defaultRegister(soapTM, defaultNillable, char.class, Soap11.ENCODED_CHAR, new CharacterType());
        defaultRegister(soapTM, defaultNillable, Character.class, Soap11.ENCODED_CHAR, new CharacterType());
        defaultRegister(soapTM, defaultNillable, String.class, Soap11.ENCODED_STRING, new StringType());
        defaultRegister(soapTM, defaultNillable, Boolean.class, Soap11.ENCODED_BOOLEAN, new BooleanType());
        defaultRegister(soapTM, defaultNillable, Integer.class, Soap11.ENCODED_INT, new IntType());
        defaultRegister(soapTM, defaultNillable, Short.class, Soap11.ENCODED_SHORT, new ShortType());
        defaultRegister(soapTM, defaultNillable, Double.class, Soap11.ENCODED_DOUBLE, new DoubleType());
        defaultRegister(soapTM, defaultNillable, Float.class, Soap11.ENCODED_FLOAT, new FloatType());
        defaultRegister(soapTM, defaultNillable, Long.class, Soap11.ENCODED_LONG, new LongType());
        defaultRegister(soapTM, defaultNillable, Date.class, Soap11.ENCODED_DATETIME, new DateTimeType());
        defaultRegister(soapTM, defaultNillable, java.sql.Date.class, Soap11.ENCODED_DATETIME,
                        new SqlDateType());
        defaultRegister(soapTM, defaultNillable, Calendar.class, Soap11.ENCODED_DATETIME, new CalendarType());
        defaultRegister(soapTM, defaultNillable, byte[].class, Soap11.ENCODED_BASE64, new Base64Type());
        defaultRegister(soapTM, defaultNillable, BigDecimal.class, Soap11.ENCODED_DECIMAL,
                        new BigDecimalType());
        defaultRegister(soapTM, defaultNillable, BigInteger.class, Soap11.ENCODED_INTEGER,
                        new BigIntegerType());

        return soapTM;
    }

    public static DefaultTypeMapping createDefaultTypeMapping(boolean defaultNillable, 
      boolean enableMtomXmime) {
        return createDefaultTypeMapping(
                                        defaultNillable,
                                        enableMtomXmime,
                                        false);
    }

    /**
     * Create a set of default type mappings.
     * @param defaultNillable whether elements are nillable by default.
     * @param enableMtomXmime whether to enable XMIME annotations on MTOM.
     * @param enableJDOM whether to map JDOM types.
     * @return
     */
    public static DefaultTypeMapping createDefaultTypeMapping(boolean defaultNillable, 
                                                              boolean enableMtomXmime, 
                                                              boolean enableJDOM) {
        // by convention, the default mapping is against the XML schema URI.
        DefaultTypeMapping tm = new DefaultTypeMapping(SOAPConstants.XSD);
        fillStandardMappings(tm, defaultNillable, enableMtomXmime, enableJDOM);
        defaultRegister(tm, defaultNillable, Character.class, 
                        CharacterAsStringType.CHARACTER_AS_STRING_TYPE_QNAME,
                        new CharacterAsStringType());
        defaultRegister(tm, defaultNillable, char.class, 
                        CharacterAsStringType.CHARACTER_AS_STRING_TYPE_QNAME,
                        new CharacterAsStringType());

        defaultRegister(tm, defaultNillable, javax.xml.datatype.Duration.class, XMLSchemaQNames.XSD_DURATION,
                            new org.jrubycxf.aegis.type.java5.DurationType());
        defaultRegister(tm, defaultNillable, javax.xml.datatype.XMLGregorianCalendar.class,
                            XMLSchemaQNames.XSD_DATE,
                            new org.jrubycxf.aegis.type.java5.XMLGregorianCalendarType());
        defaultRegister(tm, defaultNillable, javax.xml.datatype.XMLGregorianCalendar.class,
                            XMLSchemaQNames.XSD_TIME,
                            new org.jrubycxf.aegis.type.java5.XMLGregorianCalendarType());
        defaultRegister(tm, defaultNillable, javax.xml.datatype.XMLGregorianCalendar.class,
                            XMLSchemaQNames.XSD_G_DAY,
                            new org.jrubycxf.aegis.type.java5.XMLGregorianCalendarType());
        defaultRegister(tm, defaultNillable, javax.xml.datatype.XMLGregorianCalendar.class,
                            XMLSchemaQNames.XSD_G_MONTH,
                            new org.jrubycxf.aegis.type.java5.XMLGregorianCalendarType());
        defaultRegister(tm, defaultNillable, javax.xml.datatype.XMLGregorianCalendar.class,
                            XMLSchemaQNames.XSD_G_MONTH_DAY,
                            new org.jrubycxf.aegis.type.java5.XMLGregorianCalendarType());
        defaultRegister(tm, defaultNillable, javax.xml.datatype.XMLGregorianCalendar.class,
                            XMLSchemaQNames.XSD_G_YEAR,
                            new org.jrubycxf.aegis.type.java5.XMLGregorianCalendarType());
        defaultRegister(tm, defaultNillable, javax.xml.datatype.XMLGregorianCalendar.class,
                            XMLSchemaQNames.XSD_G_YEAR_MONTH,
                            new org.jrubycxf.aegis.type.java5.XMLGregorianCalendarType());
        defaultRegister(tm, defaultNillable, javax.xml.datatype.XMLGregorianCalendar.class,
                            XMLSchemaQNames.XSD_DATETIME,
                            new org.jrubycxf.aegis.type.java5.XMLGregorianCalendarType());
        return tm;
    }

    public String getMappingIdentifierURI() {
        return identifierURI;
    }

    public void setMappingIdentifierURI(String uri) {
        identifierURI = uri;
        
    }
}

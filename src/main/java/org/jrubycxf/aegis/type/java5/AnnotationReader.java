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
package org.jrubycxf.aegis.type.java5;

import org.apache.cxf.common.logging.LogUtils;
import org.jrubycxf.aegis.type.AegisType;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnnotationReader {
    private static final Logger LOG = LogUtils.getL7dLogger(AnnotationReader.class);
    private static final Class<? extends Annotation> WEB_PARAM = load("javax.jws.WebParam");
    private static final Class<? extends Annotation> WEB_RESULT = load("javax.jws.WebResult");
    private static final Class<? extends Annotation> XML_ATTRIBUTE =
            load("javax.xml.bind.annotation.XmlAttribute");
    private static final Class<? extends Annotation> XML_ELEMENT =
            load("javax.xml.bind.annotation.XmlElement");
    private static final Class<? extends Annotation> XML_SCHEMA =
            load("javax.xml.bind.annotation.XmlSchema");
    private static final Class<? extends Annotation> XML_TYPE =
            load("javax.xml.bind.annotation.XmlType");
    private static final Class<? extends Annotation> XML_TRANSIENT =
            load("javax.xml.bind.annotation.XmlTransient");

    @SuppressWarnings("unchecked")
    public boolean isIgnored(AnnotatedElement element) {
        return isAnnotationPresent(element,
                org.jrubycxf.aegis.type.java5.IgnoreProperty.class,
                XML_TRANSIENT);
    }

    @SuppressWarnings("unchecked")
    public boolean isAttribute(AnnotatedElement element) {
        return isAnnotationPresent(element,
                org.jrubycxf.aegis.type.java5.XmlAttribute.class,
                XML_ATTRIBUTE);
    }

    @SuppressWarnings("unchecked")
    public boolean isElement(AnnotatedElement element) {
        return isAnnotationPresent(element,
                org.jrubycxf.aegis.type.java5.XmlElement.class,
                XML_ELEMENT);
    }

    // PMD incorrectly identifies this as a string comparison
    @SuppressWarnings("unchecked")
    public Boolean isNillable(AnnotatedElement element) {
        return Boolean.TRUE.equals(getAnnotationValue("nillable", // NOPMD
                element,
                Boolean.FALSE,
                org.jrubycxf.aegis.type.java5.XmlElement.class,
                XML_ELEMENT));
    }
    @SuppressWarnings("unchecked")
    public static Boolean isNillable(Annotation[] anns) {
        if (anns == null) {
            return null;
        }
        return (Boolean)getAnnotationValue("nillable", // NOPMD
                anns,
                org.jrubycxf.aegis.type.java5.XmlElement.class,
                XML_ELEMENT);
    }

    @SuppressWarnings("unchecked")
    public Class<?> getType(AnnotatedElement element) {
        Class<?> value = (Class<?>) getAnnotationValue("type",
                element,
                AegisType.class,
                org.jrubycxf.aegis.type.java5.XmlAttribute.class,
                org.jrubycxf.aegis.type.java5.XmlElement.class);
        // jaxb uses a different default value
        if (value == null) {
            value = (Class<?>) getAnnotationValue("type",
                    element,
                    javax.xml.bind.annotation.XmlElement.DEFAULT.class,
                    XML_ELEMENT);
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public Class<?> getParamType(Method method, int index) {
        return (Class<?>) getAnnotationValue("type",
                method,
                index,
                AegisType.class,
                org.jrubycxf.aegis.type.java5.XmlParamType.class);
    }

    @SuppressWarnings("unchecked")
    public Class<?> getReturnType(AnnotatedElement element) {
        return (Class<?>) getAnnotationValue("type",
                element,
                AegisType.class,
                org.jrubycxf.aegis.type.java5.XmlReturnType.class);
    }

    @SuppressWarnings("unchecked")
    public String getName(AnnotatedElement element) {
        String name = (String) getAnnotationValue("name",
                element,
                "",
                org.jrubycxf.aegis.type.java5.XmlType.class,
                org.jrubycxf.aegis.type.java5.XmlAttribute.class,
                org.jrubycxf.aegis.type.java5.XmlElement.class);

        // jaxb uses a different default value
        if (name == null) {
            name = (String) getAnnotationValue("name",
                    element,
                    "##default",
                    XML_TYPE,
                    XML_ATTRIBUTE,
                    XML_ELEMENT);
        }
        return name;
    }

    @SuppressWarnings("unchecked")
    public String getParamTypeName(Method method, int index) {
        return (String) getAnnotationValue("name",
                method,
                index,
                AegisType.class,
                org.jrubycxf.aegis.type.java5.XmlParamType.class);
    }

    @SuppressWarnings("unchecked")
    public String getReturnTypeName(AnnotatedElement element) {
        return (String) getAnnotationValue("name",
                element,
                "",
                org.jrubycxf.aegis.type.java5.XmlReturnType.class);
    }

    @SuppressWarnings("unchecked")
    public String getNamespace(AnnotatedElement element) {
        // some poor class loader implementations may end not define Package elements
        if (element == null) {
            return null;
        }

        String namespace = (String) getAnnotationValue("namespace",
                element,
                "",
                org.jrubycxf.aegis.type.java5.XmlType.class,
                XmlAttribute.class,
                org.jrubycxf.aegis.type.java5.XmlElement.class,
                XML_SCHEMA);

        // jaxb uses a different default value
        if (namespace == null) {
            namespace = (String) getAnnotationValue("namespace",
                    element,
                    "##default",
                    XML_TYPE,
                    XML_ATTRIBUTE,
                    XML_ELEMENT);
        }

        return namespace;
    }

    @SuppressWarnings("unchecked")
    public String getParamNamespace(Method method, int index) {
        String namespace = (String) getAnnotationValue("namespace",
                method,
                index,
                "",
                XmlParamType.class);

        // JWS annotation field is named targetNamespace
        if (namespace == null) {
            namespace = (String) getAnnotationValue("targetNamespace", method, index, "", WEB_PARAM);
        }
        return namespace;
    }

    @SuppressWarnings("unchecked")
    public String getReturnNamespace(AnnotatedElement element) {
        String namespace = (String) getAnnotationValue("namespace",
                element,
                "",
                XmlReturnType.class);

        // JWS annotation field is named targetNamespace
        if (namespace == null) {
            namespace = (String) getAnnotationValue("targetNamespace", element, "", WEB_RESULT);
        }
        return namespace;
    }

    @SuppressWarnings("unchecked")
    public int getMinOccurs(AnnotatedElement element) {
        String minOccurs = (String) getAnnotationValue("minOccurs",
                element,
                "",
                org.jrubycxf.aegis.type.java5.XmlElement.class);
        if (minOccurs != null) {
            return Integer.parseInt(minOccurs);
        }

        // check jaxb annotation
        Boolean required = (Boolean) getAnnotationValue("required", element, null, XML_ELEMENT);
        if (Boolean.TRUE.equals(required)) {
            return 1;
        }

        return 0;
    }
    @SuppressWarnings("unchecked")
    public static Integer getMinOccurs(Annotation[] anns) {
        if (anns == null) {
            return null;
        }
        String minOccurs = (String) getAnnotationValue("minOccurs",
                anns,
                XmlElement.class);
        if (minOccurs != null) {
            return Integer.valueOf(minOccurs);
        }

        // check jaxb annotation
        Boolean required = (Boolean) getAnnotationValue("required", anns, XML_ELEMENT);
        if (Boolean.TRUE.equals(required)) {
            return 1;
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public boolean isExtensibleElements(AnnotatedElement element, boolean defaultValue) {
        Boolean extensibleElements = (Boolean) getAnnotationValue("extensibleElements",
                element,
                Boolean.TRUE,
                org.jrubycxf.aegis.type.java5.XmlType.class);

        if (extensibleElements == null) {
            return defaultValue;
        }
        return extensibleElements;
    }

    @SuppressWarnings("unchecked")
    public boolean isExtensibleAttributes(AnnotatedElement element, boolean defaultValue) {
        Boolean extensibleAttributes = (Boolean) getAnnotationValue("extensibleAttributes",
                element,
                Boolean.TRUE,
                XmlType.class);

        if (extensibleAttributes == null) {
            return defaultValue;
        }
        return extensibleAttributes;
    }

    // PMD doesn't fully understand varargs
    private static boolean isAnnotationPresent(AnnotatedElement element, // NOPMD
            Class<? extends Annotation>... annotations) {
        for (Class<?> annotation : annotations) {
            if (annotation != null && element.isAnnotationPresent(annotation.asSubclass(Annotation.class))) {
                return true;
            }
        }
        return false;
    }

    static Object getAnnotationValue(String name,
            AnnotatedElement element,
            Object ignoredValue,
            Class<? extends Annotation>... annotations) {

        for (Class<?> annotation : annotations) {
            if (annotation != null) {
                try {
                    Annotation ann = element.getAnnotation(annotation.asSubclass(Annotation.class));
                    if (ann != null) {
                        Method method = ann.getClass().getMethod(name);
                        Object value = method.invoke(ann);
                        if ((ignoredValue == null && value != null) || (ignoredValue != null
                                && !ignoredValue.equals(value))) {
                            return value;
                        }
                    }
                } catch (Exception ignored) {
                    // annotation did not have value
                }
            }
        }
        return null;
    }
    static Object getAnnotationValue(String name,
                                     Annotation[] anns,
                                     Class<? extends Annotation>... annotations) {
        for (Class<?> annotation : annotations) {
            if (annotation != null) {
                try {
                    for (Annotation ann : anns) {
                        if (annotation.isInstance(ann)) {
                            Method method = ann.getClass().getMethod(name);
                            return method.invoke(ann);
                        }
                    }
                } catch (Exception ignored) {
                    // annotation did not have value
                }
            }
        }
        return null;
    }

    static Object getAnnotationValue(String name,
            Method method,
            int index,
            Object ignoredValue,
            Class<? extends Annotation>... annotations) {

        if (method.getParameterAnnotations() == null
            || method.getParameterAnnotations().length <= index
            || method.getParameterAnnotations()[index] == null) {
            return null;
        }

        for (Class<? extends Annotation> annotation : annotations) {
            if (annotation != null) {
                try {
                    Annotation ann = getAnnotation(method, index, annotation);
                    if (ann != null) {
                        Object value = ann.getClass().getMethod(name).invoke(ann);
                        if ((ignoredValue == null && value != null) || (ignoredValue != null
                                && !ignoredValue.equals(value))) {
                            return value;
                        }
                    }
                } catch (Exception ignored) {
                    // annotation did not have value
                }
            }
        }
        return null;
    }

    private static Annotation getAnnotation(Method method, int index, Class<? extends Annotation> type) {
        if (method.getParameterAnnotations() == null
            || method.getParameterAnnotations().length <= index
            || method.getParameterAnnotations()[index] == null) {
            return null;
        }

        Annotation[] annotations = method.getParameterAnnotations()[index];
        for (Annotation annotation : annotations) {
            if (type.isInstance(annotation)) {
                return annotation;
            }
        }
        return null;
    }

    private static Class<? extends Annotation> load(String name) {
        try {
            return AnnotationReader.class.getClassLoader().loadClass(name).asSubclass(Annotation.class);
        } catch (Throwable e) {
            LOG.log(Level.FINE, "Error loading annotation class " + name + ".", e);
            return null;
        }
    }

    public boolean isFlat(Annotation[] annotations) {
        if (annotations != null) {
            for (Annotation a : annotations) {
                if (a instanceof XmlFlattenedArray) {
                    return true;
                }
            }
        }
        return false;
    }


}

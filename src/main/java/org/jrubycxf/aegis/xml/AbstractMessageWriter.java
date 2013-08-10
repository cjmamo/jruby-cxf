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
package org.jrubycxf.aegis.xml;

import org.apache.cxf.common.util.SOAPConstants;

import javax.xml.namespace.QName;

/**
 * Basic type conversion functionality for writing messages.
 */
public abstract class AbstractMessageWriter implements org.jrubycxf.aegis.xml.MessageWriter {
    
    private boolean xsiTypeWritten;
    
    public AbstractMessageWriter() {
    }

    public void writeXsiType(QName type) {
        if (xsiTypeWritten) {
            return;
        }
        xsiTypeWritten = true;

        /*
         * Do not assume that the prefix supplied with the QName should be used
         * in this case.
         */
        String prefix = getPrefixForNamespace(type.getNamespaceURI(), type.getPrefix());
        String value;
        if (prefix != null && prefix.length() > 0) {
            StringBuilder sb = new StringBuilder(prefix.length() + 1 + type.getLocalPart().length());
            sb.append(prefix);
            sb.append(':');
            sb.append(type.getLocalPart());
            value = sb.toString();
        } else {
            value = type.getLocalPart();
        }
        getAttributeWriter("type", SOAPConstants.XSI_NS).writeValue(value);
    }

    public void writeXsiNil() {
        org.jrubycxf.aegis.xml.MessageWriter attWriter = getAttributeWriter("nil", SOAPConstants.XSI_NS);
        attWriter.writeValue("true");
        attWriter.close();
    }

    /**
     * @see org.jrubycxf.aegis.xml.MessageWriter#writeValueAsInt(Integer)
     */
    public void writeValueAsInt(Integer i) {
        writeValue(i.toString());
    }
    
    public void writeValueAsByte(Byte b) {
        writeValue(b.toString());
    }

    /**
     * @see org.jrubycxf.aegis.xml.MessageWriter#writeValueAsDouble(Double)
     */
    public void writeValueAsDouble(Double d) {
        writeValue(d.toString());
    }

    /**
     * @see org.jrubycxf.aegis.xml.MessageWriter#writeValueAsCharacter(Character)
     */
    public void writeValueAsCharacter(Character char1) {
        writeValue(char1.toString());
    }

    /**
     * @see org.jrubycxf.aegis.xml.MessageWriter#writeValueAsLong(Long)
     */
    public void writeValueAsLong(Long l) {
        writeValue(l.toString());
    }

    /**
     * @see org.jrubycxf.aegis.xml.MessageWriter#writeValueAsFloat(Float)
     */
    public void writeValueAsFloat(Float f) {
        writeValue(f.toString());
    }

    /**
     * @see org.jrubycxf.aegis.xml.MessageWriter#writeValueAsBoolean(boolean)
     */
    public void writeValueAsBoolean(boolean b) {
        writeValue(b ? "true" : "false");
    }

    public void writeValueAsShort(Short s) {
        writeValue(s.toString());
    }
}

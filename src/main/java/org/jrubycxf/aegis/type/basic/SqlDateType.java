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
package org.jrubycxf.aegis.type.basic;

import java.util.Date;

import org.jrubycxf.aegis.Context;
import org.jrubycxf.aegis.DatabindingException;
import org.jrubycxf.aegis.xml.MessageReader;
import org.jrubycxf.aegis.xml.MessageWriter;

/**
 * AegisType for the java.sql.Date class which serializes as an xsd:date (no time
 * information).
 */
public class SqlDateType extends org.jrubycxf.aegis.type.basic.DateType {
    @Override
    public Object readObject(MessageReader reader, Context context) throws DatabindingException {
        Date date = (Date)super.readObject(reader, context);
        if (date == null) {
            return null;
        }

        return new java.sql.Date(date.getTime());
    }

    @Override
    public void writeObject(Object object, MessageWriter writer, Context context) {
        java.sql.Date date = (java.sql.Date)object;

        super.writeObject(new Date(date.getTime()), writer, context);
    }
}

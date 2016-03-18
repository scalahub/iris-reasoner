/*
 * Copyright 2010 STI Innsbruck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deri.iris.api.terms.concrete;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines the list of XML Schema datatypes.
 * 
 * @author Adrian Marte
 */
public enum XmlSchemaDatatype {

	// Primitive data types.

	STRING("string"),

	BOOLEAN("boolean"),

	DECIMAL("decimal"),

	FLOAT("float"),

	DOUBLE("double"),

	DURATION("duration"),

	DAYTIMEDURATION("dayTimeDuration"),

	YEARMONTHDURATION("yearMonthDuration"),

	DATETIME("dateTime"),

	DATETIMESTAMP("dateTimeStamp"),

	TIME("time"),

	DATE("date"),

	GYEARMONTH("gYearMonth"),

	GYEAR("gYear"),

	GMONTHDAY("gMonthDay"),

	GDAY("gDay"),

	GMONTH("gMonth"),

	HEXBINARY("hexBinary"),

	BASE64BINARY("base64Binary"),

	ANYURI("anyURI"),

	QNAME("QName"),

	NOTATION("NOTATION"),

	// Derived data types.

	NORMALIZED_STRING("normalizedString"),

	TOKEN("token"),

	LANGUAGE("language"),

	NMTOKEN("NMTOKEN"),

	NMTOKENS("NMTOKENS"),

	NAME("Name"),

	NCNAME("NCName"),

	ID("ID"),

	IDREF("IDREF"),

	IDREFS("IDREFS"),

	ENTITY("ENTITY"),

	ENTITIES("ENTITIES"),

	INTEGER("integer"),

	NON_POSITIVE_INTEGER("nonPositiveInteger"),

	NEGATIVE_INTEGER("negativeInteger"),

	LONG("long"),

	INT("int"),

	SHORT("short"),

	BYTE("byte"),

	NON_NEGATIVE_INTEGER("nonNegativeInteger"),

	UNSIGNED_LONG("unsignedLong"),

	UNSIGNED_INT("unsignedInt"),

	UNSIGNED_SHORT("unsignedShort"),

	UNSIGNED_BYTE("unsignedByte"),

	POSITIVE_INTEGER("positiveInteger");

	/**
	 * The XML schema namespace.
	 */
	public static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema#";

	private String namespace;

	private String uri;

	private String name;

	private XmlSchemaDatatype(String name) {
		this(XSD_NAMESPACE, name);
	}

	private XmlSchemaDatatype(String namespace, String name) {
		this.namespace = namespace;
		this.name = name;
		this.uri = namespace + name;
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getUri() {
		return this.uri;
	}

	public static XmlSchemaDatatype forUri(String uri) {
		return Lookup.table.get(uri);
	}

	public static boolean isDatatype(String uri) {
		return Lookup.table.containsKey(uri);
	}

	@Override
	public String toString() {
		return getUri();
	}
	
	public URI toUri() {
		return URI.create(getUri());
	}

	/**
	 * Maps the URI of each datatype to the datatype itself.
	 */
	private static class Lookup {

		private static final Map<String, XmlSchemaDatatype> table;

		static {
			table = new HashMap<String, XmlSchemaDatatype>();

			for (XmlSchemaDatatype builtin : XmlSchemaDatatype.values()) {
				table.put(builtin.getUri(), builtin);
			}
		}

	}

}

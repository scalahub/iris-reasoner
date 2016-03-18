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
package org.deri.iris.terms.concrete;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.deri.iris.api.factory.IConcreteFactory;
import org.deri.iris.api.factory.ITermFactory;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.concrete.RdfDatatype;
import org.deri.iris.api.terms.concrete.RifDatatype;
import org.deri.iris.api.terms.concrete.XmlSchemaDatatype;
import org.deri.iris.factory.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates terms for the canonical string representation of a data value.
 * 
 * @author Adrian Marte
 */
public class CanonicalFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(CanonicalFactory.class);

	private ITermFactory termFactory;

	private IConcreteFactory factory;

	public CanonicalFactory() {
		this(Factory.TERM, Factory.CONCRETE);
	}

	public CanonicalFactory(ITermFactory termFactory, IConcreteFactory factory) {
		this.termFactory = termFactory;
		this.factory = factory;
	}

	/**
	 * Creates a term for the specified canonical string representation of a
	 * data value of the specified datatype.
	 * 
	 * @param value
	 *            The canonical representation of the data value.
	 * @param type
	 *            The data type URI of the data value.
	 * @return The corresponding term for the data value, or <code>null</code>
	 *         if there is no corresponding term.
	 * @throws IllegalArgumentException
	 *             If the value is not compatible with the type.
	 */
	public ITerm createTerm(String value, String type) {
		// Check if this is a XSD datatype.
		XmlSchemaDatatype xsdDatatype = XmlSchemaDatatype.forUri(type);

		if (xsdDatatype != null) {
			switch (xsdDatatype) {
			case ANYURI:
				return factory.createAnyURI(URI.create(value));
			case BASE64BINARY:
				return factory.createBase64Binary(value);
			case BOOLEAN:
				return factory.createBoolean(value);
			case BYTE:
				return factory.createByte(Byte.valueOf(value));
			case DATE:
				DateTime date = DateTime.parseDate(value);

				if (date != null) {
					return factory.createDate(date.year, date.month, date.day,
							date.tzHour, date.tzMinute);
				}

				break;
			case DATETIME:
				DateTime dateTime = DateTime.parseDateTime(value);

				if (dateTime != null) {
					return factory.createDateTime(dateTime.year,
							dateTime.month, dateTime.day, dateTime.hour,
							dateTime.minute, dateTime.second, dateTime.tzHour,
							dateTime.tzMinute);
				}

				break;
			case DATETIMESTAMP:
				DateTime dateTimeStamp = DateTime.parseDateTime(value);

				if (dateTimeStamp != null) {
					return factory.createDateTimeStamp(dateTimeStamp.year,
							dateTimeStamp.month, dateTimeStamp.day,
							dateTimeStamp.hour, dateTimeStamp.minute,
							dateTimeStamp.second, dateTimeStamp.tzHour,
							dateTimeStamp.tzMinute);
				}

				break;
			case DAYTIMEDURATION:
				Duration dayTimeDuration = createDuration(value);

				if (dayTimeDuration != null) {
					return factory.createDayTimeDuration(
							dayTimeDuration.getSign() == 1,
							dayTimeDuration.getDays(),
							dayTimeDuration.getHours(),
							dayTimeDuration.getMinutes(),
							dayTimeDuration.getSeconds());
				}

				break;
			case DECIMAL:
				return factory.createDecimal(new BigDecimal(value));
			case DOUBLE:
				return factory.createDouble(Double.valueOf(value));
			case DURATION:
				Duration duration = createDuration(value);

				if (duration != null) {
					return factory.createDuration(duration.getSign() == 1,
							duration.getYears(), duration.getMonths(),
							duration.getDays(), duration.getHours(),
							duration.getMinutes(), duration.getSeconds());
				}

				break;
			case ENTITIES:
				// FIXME
				break;
			case ENTITY:
				return factory.createEntity(value);
			case FLOAT:
				return factory.createFloat(floatValueOf(value));
			case GDAY:
				String[] gregorian = extractGregorian(value);
				return factory.createGDay(Integer.valueOf(gregorian[0]));
			case GMONTH:
				gregorian = extractGregorian(value);
				return factory.createGMonth(Integer.valueOf(gregorian[0]));
			case GMONTHDAY:
				gregorian = extractGregorian(value);
				return factory.createGMonthDay(Integer.valueOf(gregorian[0]),
						Integer.valueOf(gregorian[1]));
			case GYEAR:
				gregorian = extractGregorian(value);
				return factory.createGYear(Integer.valueOf(gregorian[0]));
			case GYEARMONTH:
				gregorian = extractGregorian(value);
				return factory.createGYearMonth(Integer.valueOf(gregorian[0]),
						Integer.valueOf(gregorian[1]));
			case HEXBINARY:
				return factory.createHexBinary(value);
			case ID:
				return factory.createID(value);
			case IDREF:
				return factory.createIDREF(value);
			case IDREFS:
				// FIXME
				break;
			case INT:
				return factory.createInt(Integer.valueOf(value));
			case INTEGER:
				int index = value.indexOf(".");

				// Ignore fractional part if it exists.
				if (index >= 0) {
					value = value.substring(0, index);
				}

				return factory.createInteger(new BigInteger(value));
			case LANGUAGE:
				return factory.createLanguage(value);
			case LONG:
				return factory.createLong(Long.valueOf(value));
			case NAME:
				return factory.createName(value);
			case NCNAME:
				return factory.createNCName(value);
			case NEGATIVE_INTEGER:
				return factory.createNegativeInteger(new BigInteger(value));
			case NMTOKEN:
				return factory.createNMTOKEN(value);
			case NMTOKENS:
				// FIXME IRIS data type missing.
				break;
			case NON_NEGATIVE_INTEGER:
				return factory.createNonNegativeInteger(new BigInteger(value));
			case NON_POSITIVE_INTEGER:
				return factory.createPositiveInteger(new BigInteger(value));
			case NORMALIZED_STRING:
				return factory.createNormalizedString(value);
			case NOTATION:
				// FIXME Extract values form string.
				break;
			case POSITIVE_INTEGER:
				return factory.createPositiveInteger(new BigInteger(value));
			case QNAME:
				String[] parts = value.split(":");

				if (parts.length == 2) {
					String namespaceName = parts[0];
					String localPart = parts[1];

					return factory.createQName(namespaceName, localPart);
				}

				break;
			case SHORT:
				return factory.createShort(Short.valueOf(value));
			case STRING:
				return termFactory.createString(value);
			case TIME:
				DateTime time = DateTime.parseTime(value);

				if (time != null) {
					return factory.createTime(time.hour, time.minute,
							time.second, time.tzHour, time.tzMinute);
				}

				break;
			case TOKEN:
				return factory.createToken(value);
			case UNSIGNED_BYTE:
				return factory.createUnsignedByte(Short.valueOf(value));
			case UNSIGNED_INT:
				return factory.createUnsignedInt(Long.valueOf(value));
			case UNSIGNED_LONG:
				return factory.createUnsignedLong(new BigInteger(value));
			case UNSIGNED_SHORT:
				return factory.createUnsignedShort(Integer.valueOf(value));
			case YEARMONTHDURATION:
				Duration yearMonthDuration = createDuration(value);

				if (yearMonthDuration != null) {
					return factory.createYearMonthDuration(
							yearMonthDuration.getSign() == 1,
							yearMonthDuration.getYears(),
							yearMonthDuration.getMonths());
				}

				break;
			}
		}

		// Check if this is a RDF datatype.
		RdfDatatype rdfDatatype = RdfDatatype.forUri(type);

		if (rdfDatatype != null) {
			switch (rdfDatatype) {
			case PLAIN_LITERAL:
				int position = value.lastIndexOf("@");

				String text = value;
				String lang = "";

				if (position >= 0) {
					text = value.substring(0, position);
					lang = "";

					if (position < (value.length() - 1)) {
						lang = value.substring(position + 1);
					}
				}

				return factory.createPlainLiteral(text, lang);
			case XML_LITERAL:
				// TODO What about the language?
				return factory.createXMLLiteral(value);
			}
		}

		// Check if this is a RIF datatype.
		RifDatatype rifDatatype = RifDatatype.forUri(type);

		if (rifDatatype != null) {
			switch (rifDatatype) {
			case IRI:
				return Factory.CONCRETE.createIri(value);
			case LOCAL:
				// TODO Implement.
				break;
			}
		}

		logger.error("Could not find appropriate IRIS data type for " + type);

		return null;
	}

	private Duration createDuration(String value) {
		DatatypeFactory foo;
		try {
			foo = DatatypeFactory.newInstance();
			Duration duration = foo.newDuration(value);
			return duration;
		} catch (DatatypeConfigurationException e) {
		} catch (IllegalArgumentException e) {
		}

		return null;
	}

	private String[] extractGregorian(String value) {
		String[] values = value.split("Z");

		value = values[0].substring(2);
		return value.split("-");
	}

	/**
	 * A helper class to parse the lexical representation of the date and time
	 * data types.
	 * 
	 * @author Adrian Marte
	 */
	private static class DateTime {
		public int year;

		public int month;

		public int day;

		public int hour;

		public int minute;

		public double second;

		public int tzHour;

		public int tzMinute;

		// Per default 1, indicating positive time/timezone.
		@SuppressWarnings("unused")
		public int sign = 1;

		public static DateTime parseDateTime(String value) {
			DateTime timezone = extractTimezone(value);
			value = removeTimezone(value);
			int sign = value.startsWith("-") ? -1 : 1;

			String[] parts = value.split("T");

			if (parts.length >= 2) {
				DateTime date = parseDate(parts[0]);
				DateTime time = parseTime(parts[1]);
				timezone.sign = sign;

				if (date != null && time != null) {
					timezone.year = date.year;
					timezone.month = date.month;
					timezone.day = date.day;
					timezone.hour = time.hour;
					timezone.minute = time.minute;
					timezone.second = time.second;

					return timezone;
				}
			}

			return null;
		}

		public static DateTime parseDate(String value) {
			DateTime timezone = extractTimezone(value);
			value = removeTimezone(value);

			int sign = value.startsWith("-") ? -1 : 1;

			String[] parts = value.split("-");

			if (parts.length == 3) {
				timezone.sign = sign;
				timezone.year = Integer.valueOf(parts[0]);
				timezone.month = Integer.valueOf(parts[1]);
				timezone.day = Integer.valueOf(parts[2]);

				return timezone;
			}

			return null;
		}

		public static DateTime parseTime(String value) {
			DateTime timezone = extractTimezone(value);
			value = removeTimezone(value);

			int sign = value.startsWith("-") ? -1 : 1;

			String[] parts = value.split(":");

			if (parts.length == 3) {
				timezone.sign = sign;
				timezone.hour = Integer.valueOf(parts[0]);
				timezone.minute = Integer.valueOf(parts[1]);
				timezone.second = Double.valueOf(parts[2]);

				return timezone;
			}

			return null;
		}

		public static DateTime extractTimezone(String value) {
			if (value.endsWith("Z")) {
				value = value.substring(0, value.length() - 1);
			}

			DateTime dateTime = new DateTime();
			dateTime.tzHour = 0;
			dateTime.tzMinute = 0;

			int indexOfPlus = value.lastIndexOf("+");
			int indexOfMinus = value.lastIndexOf("-");

			String timezonePart = null;

			// FIXME Prepend sign to values?
			String sign = "";

			int theSign = 1;

			if (indexOfPlus >= 0) {
				timezonePart = value.substring(indexOfPlus + 1);
			} else if (indexOfMinus >= 0) {
				timezonePart = value.substring(indexOfMinus + 1);
				theSign = -1;
				sign = "-";
			} else {
				return dateTime;
			}

			String[] parts = timezonePart.split(":");
			dateTime.sign = theSign;

			if (parts.length == 2) {
				dateTime.tzHour = Integer.valueOf(sign + parts[0]);
				dateTime.tzMinute = Integer.valueOf(sign + parts[1]);
			}

			return dateTime;
		}

		public static String removeTimezone(String value) {
			if (value.endsWith("Z")) {
				return value.substring(0, value.length() - 1);
			}

			int indexOfPlus = value.lastIndexOf("+");
			int indexOfMinus = value.lastIndexOf("-");

			String timezonePart = null;

			if (indexOfPlus >= 0) {
				timezonePart = value.substring(indexOfPlus + 1);
			} else if (indexOfMinus >= 0) {
				timezonePart = value.substring(indexOfMinus + 1);
			} else {
				return value;
			}

			String[] parts = timezonePart.split(":");

			if (parts.length == 2) {
				int position = value.lastIndexOf(timezonePart);
				return value.substring(0, position - 1);
			}

			return value;
		}
	}

	public static float floatValueOf(String value) {
		// INF, -INF and NaN
		if (value.equals("INF")) {
			return Float.POSITIVE_INFINITY;
		} else if (value.equals("-INF")) {
			return Float.NEGATIVE_INFINITY;
		} else if (value.equals("NaN")) {
			return Float.NaN;
		} else {
			return Float.valueOf(value);
		}
	}

}
